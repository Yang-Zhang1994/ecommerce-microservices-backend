package main

import (
	"bytes"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"sort"
	"strings"
	"time"

	"gopkg.in/yaml.v3"
)

const (
	kindCluster    = "gulimall"
	kubeContext    = "kind-gulimall"
	sandboxNS      = "gm-sandbox"
	sandboxRelease = "gm-sandbox"
	chartDir       = "k8s/helm/gulimall"
)

var runCmd = func(dir, name string, args ...string) (string, error) {
	cmd := exec.Command(name, args...)
	cmd.Dir = dir
	var out bytes.Buffer
	cmd.Stdout, cmd.Stderr = &out, &out
	err := cmd.Run()
	return out.String(), err
}

func renderChart(root, env string) (string, error) {
	args := []string{"template", "gm", chartDir, "-f", chartDir + "/values.yaml", "-f", chartDir + "/values-" + env + ".yaml"}
	if env == "eks" {
		args = append(args, "--set", "global.imageTag=ci") // CD always pins the git SHA
	}
	return runCmd(root, "helm", args...)
}

// Policy renders the chart for each environment and runs the guardrails on it.
func Policy(root string, w io.Writer) error {
	failed := false
	for _, env := range []string{"kind", "eks"} {
		manifest, err := renderChart(root, env)
		if err != nil {
			return fmt.Errorf("helm template (%s): %v\n%s", env, err, manifest)
		}
		cmd := exec.Command("conftest", "test", "--no-color", "--policy", "platform/policy", "-")
		cmd.Dir, cmd.Stdin = root, strings.NewReader(manifest)
		out, err := cmd.CombinedOutput()
		lines := strings.Split(strings.TrimSpace(string(out)), "\n")
		fmt.Fprintf(w, "%-4s %s\n", env, lines[len(lines)-1])
		for _, l := range lines {
			if strings.HasPrefix(l, "FAIL") {
				fmt.Fprintln(w, "     "+strings.TrimPrefix(l, "FAIL - - main - "))
			}
		}
		failed = failed || err != nil
	}
	if failed {
		return fmt.Errorf("guardrails failed")
	}
	return nil
}

type check struct {
	name string
	run  func() (ok bool, detail, fix string)
}

// Doctor runs every preflight check and prints a fix for each failure. It never changes anything.
func Doctor(root string, w io.Writer) bool {
	checks := []check{}
	for _, tool := range []string{"docker", "kind", "kubectl", "helm", "mvn", "conftest"} {
		checks = append(checks, check{"tool: " + tool, func() (bool, string, string) {
			if _, err := exec.LookPath(tool); err != nil {
				return false, "not on PATH", "brew install " + brewName(tool)
			}
			return true, "", ""
		}})
	}
	checks = append(checks,
		check{"docker daemon", func() (bool, string, string) {
			if out, err := runCmd(root, "docker", "info", "--format", "{{.ServerVersion}}"); err != nil {
				return false, firstLine(out), "start Docker Desktop"
			}
			return true, "", ""
		}},
		check{"kind cluster " + kindCluster, func() (bool, string, string) {
			out, _ := runCmd(root, "kind", "get", "clusters")
			if !regexp.MustCompile(`(?m)^` + kindCluster + `$`).MatchString(out) {
				return false, "not found", "k8s/scripts/kind-up.sh"
			}
			return true, "", ""
		}},
		check{"kube API reachable (" + kubeContext + ")", func() (bool, string, string) {
			if out, err := runCmd(root, "kubectl", "--context", kubeContext, "get", "--raw", "/readyz"); err != nil {
				return false, firstLine(out), "docker start " + kindCluster + "-control-plane"
			}
			return true, "", ""
		}},
		check{"chart lints", func() (bool, string, string) {
			if out, err := runCmd(root, "helm", "lint", chartDir, "-f", chartDir+"/values-kind.yaml"); err != nil {
				return false, lastLine(out), "helm lint " + chartDir + " for details"
			}
			return true, "", ""
		}},
		check{"workload guardrails", func() (bool, string, string) {
			var buf bytes.Buffer
			if err := Policy(root, &buf); err != nil {
				return false, violationCount(buf.String()), "gm policy  (lists each violation)"
			}
			return true, "", ""
		}},
		check{"every service registered everywhere", func() (bool, string, string) {
			missing := registrationGaps(root)
			if len(missing) > 0 {
				return false, strings.Join(missing, "; "), "add the missing entries, or create services with gm new service"
			}
			return true, "", ""
		}},
	)
	allOK := true
	for _, c := range checks {
		ok, detail, fix := c.run()
		mark := "ok  "
		if !ok {
			mark, allOK = "FAIL", false
		}
		fmt.Fprintf(w, "%s %s", mark, c.name)
		if detail != "" {
			fmt.Fprintf(w, ": %s", detail)
		}
		fmt.Fprintln(w)
		if fix != "" {
			fmt.Fprintf(w, "     fix: %s\n", fix)
		}
	}
	return allOK
}

func brewName(tool string) string {
	if tool == "mvn" {
		return "maven"
	}
	return tool
}

func violationCount(s string) string {
	return fmt.Sprintf("%d violations", strings.Count(s, "\n     "))
}

// registrationGaps finds Helm apps that are missing a module folder, Dockerfile, pom entry or kind build entry.
func registrationGaps(root string) []string {
	var v struct {
		Apps map[string]struct {
			Name    string `yaml:"name"`
			Enabled bool   `yaml:"enabled"`
		} `yaml:"apps"`
	}
	raw, _ := os.ReadFile(filepath.Join(root, valuesPath))
	pom, _ := os.ReadFile(filepath.Join(root, pomPath))
	common, _ := os.ReadFile(filepath.Join(root, commonPath))
	if yaml.Unmarshal(raw, &v) != nil {
		return []string{valuesPath + " does not parse"}
	}
	var gaps []string
	for _, app := range v.Apps {
		m := app.Name
		if _, err := os.Stat(filepath.Join(root, m, "Dockerfile")); err != nil {
			gaps = append(gaps, m+": no Dockerfile")
		}
		if !strings.Contains(string(pom), "<module>"+m+"</module>") {
			gaps = append(gaps, m+": not in pom.xml")
		}
		if strings.Count(string(common), "\n  "+m+"\n") < 2 {
			gaps = append(gaps, m+": not in both kind build lists")
		}
	}
	sort.Strings(gaps)
	return gaps
}

// SandboxDeploy builds one service and deploys it alone (plus Consul) into gm-sandbox, leaving the
// main gulimall release untouched.
func SandboxDeploy(root, name string) error {
	var v struct {
		Apps map[string]struct {
			Name  string `yaml:"name"`
			Redis bool   `yaml:"redis"`
		} `yaml:"apps"`
	}
	raw, err := os.ReadFile(filepath.Join(root, valuesPath))
	if err != nil {
		return err
	}
	if err := yaml.Unmarshal(raw, &v); err != nil {
		return err
	}
	app, ok := v.Apps[name]
	if !ok {
		return fmt.Errorf("no app %q in %s", name, valuesPath)
	}
	start := time.Now()
	platform := os.Getenv("DOCKER_PLATFORM")
	for _, s := range []struct {
		label string
		cmd   []string
	}{
		{"maven package", []string{"mvn", "-q", "-pl", app.Name, "-am", "package", "-DskipTests"}},
		{"docker build", append([]string{"docker", "build", "-q", "-t", app.Name + ":local"}, append(platformFlag(platform), app.Name)...)},
	} {
		if err := step(root, s.label, s.cmd); err != nil {
			return err
		}
	}
	// Tag each build by its image ID: reusing ":local" would leave the old pod running after a rebuild.
	id, err := runCmd(root, "docker", "image", "inspect", "-f", "{{.Id}}", app.Name+":local")
	if err != nil {
		return fmt.Errorf("docker image inspect: %v\n%s", err, id)
	}
	tag := "sbx-" + strings.TrimPrefix(strings.TrimSpace(id), "sha256:")[:12]
	steps := []struct {
		label string
		cmd   []string
	}{
		{"docker tag " + tag, []string{"docker", "tag", app.Name + ":local", app.Name + ":" + tag}},
		{"kind load", []string{"kind", "load", "docker-image", app.Name + ":" + tag, "--name", kindCluster}},
	}
	set := []string{"--set", "apps." + name + ".enabled=true", "--set", "images." + name + ".tag=" + tag,
		"--set", "infra.consul.seedConfig=false",
		"--set", "infra.rabbitmq.enabled=false", "--set", "infra.elasticsearch.enabled=false",
		"--set", "infra.jaeger.enabled=false", "--set", fmt.Sprintf("infra.redis.enabled=%v", app.Redis),
		"--set", "global.deploymentStrategy=Recreate"}
	for key := range v.Apps {
		if key != name {
			set = append(set, "--set", "apps."+key+".enabled=false")
		}
	}
	helm := append([]string{"helm", "--kube-context", kubeContext, "upgrade", "--install", sandboxRelease, chartDir,
		"-n", sandboxNS, "--create-namespace", "-f", chartDir + "/values.yaml", "-f", chartDir + "/values-kind.yaml",
		"--wait", "--timeout", "6m"}, set...)
	steps = append(steps, struct {
		label string
		cmd   []string
	}{"helm install into " + sandboxNS, helm})
	for _, s := range steps {
		if err := step(root, s.label, s.cmd); err != nil {
			return err
		}
	}
	fmt.Printf("%s is ready in %s after %.0fs. Try:\n  kubectl --context %s -n %s port-forward svc/%s 8080:%s\n",
		app.Name, sandboxNS, time.Since(start).Seconds(), kubeContext, sandboxNS, app.Name, portOf(raw, name))
	return nil
}

func step(root, label string, cmd []string) error {
	t := time.Now()
	fmt.Printf("==> %s ... ", label)
	if out, err := runCmd(root, cmd[0], cmd[1:]...); err != nil {
		fmt.Println("failed")
		return fmt.Errorf("%s: %v\n%s", label, err, out)
	}
	fmt.Printf("%.0fs\n", time.Since(t).Seconds())
	return nil
}

func SandboxDelete() error {
	out, err := runCmd(".", "kubectl", "--context", kubeContext, "delete", "namespace", sandboxNS, "--ignore-not-found")
	fmt.Print(out)
	return err
}

func platformFlag(p string) []string {
	if p == "" {
		return nil
	}
	return []string{"--platform", p}
}

func portOf(values []byte, name string) string {
	var v struct {
		Apps map[string]struct {
			Port int `yaml:"port"`
		} `yaml:"apps"`
	}
	_ = yaml.Unmarshal(values, &v)
	return fmt.Sprint(v.Apps[name].Port)
}

func firstLine(s string) string { return strings.SplitN(strings.TrimSpace(s), "\n", 2)[0] }

func lastLine(s string) string {
	lines := strings.Split(strings.TrimSpace(s), "\n")
	return lines[len(lines)-1]
}
