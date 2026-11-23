package main

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"testing"

	"gopkg.in/yaml.v3"
)

// tempRepo copies the real files gm edits (pom.xml, the chart, common.sh) into a temp dir, so the tests
// run against the repository as it is rather than hand-made fixtures.
func tempRepo(t *testing.T) string {
	t.Helper()
	src, _ := filepath.Abs("../..")
	dst := t.TempDir()
	for _, rel := range []string{pomPath, commonPath} {
		copyFile(t, filepath.Join(src, rel), filepath.Join(dst, rel))
	}
	err := filepath.Walk(filepath.Join(src, chartDir), func(p string, info os.FileInfo, err error) error {
		if err != nil || info.IsDir() {
			return err
		}
		rel, _ := filepath.Rel(src, p)
		copyFile(t, p, filepath.Join(dst, rel))
		return nil
	})
	if err != nil {
		t.Fatal(err)
	}
	// registrationGaps looks for each module's Dockerfile.
	var v struct {
		Apps map[string]struct{ Name string } `yaml:"apps"`
	}
	raw, _ := os.ReadFile(filepath.Join(dst, valuesPath))
	_ = yaml.Unmarshal(raw, &v)
	for _, a := range v.Apps {
		write(t, filepath.Join(dst, a.Name, "Dockerfile"), "FROM scratch\n")
	}
	return dst
}

func copyFile(t *testing.T, from, to string) {
	t.Helper()
	b, err := os.ReadFile(from)
	if err != nil {
		t.Fatal(err)
	}
	write(t, to, string(b))
}

func write(t *testing.T, path, content string) {
	t.Helper()
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		t.Fatal(err)
	}
	if err := os.WriteFile(path, []byte(content), 0o644); err != nil {
		t.Fatal(err)
	}
}

func read(t *testing.T, path string) string {
	t.Helper()
	b, err := os.ReadFile(path)
	if err != nil {
		t.Fatal(err)
	}
	return string(b)
}

func TestNewServiceValidatesNameAndPort(t *testing.T) {
	for _, bad := range []string{"Review", "review_svc", "-review", "review-", "a--b", "gulimall-loyalty", "averyveryverylongname1"} {
		if _, err := NewService(bad, 15000, false, ""); err == nil {
			t.Errorf("name %q should be refused", bad)
		}
	}
	for _, port := range []int{80, 70000} {
		if _, err := NewService("review", port, false, ""); err == nil {
			t.Errorf("port %d should be refused", port)
		}
	}
	s, err := NewService("gift-card", 15000, false, "")
	if err != nil {
		t.Fatal(err)
	}
	if s.Module != "gulimall-gift-card" || s.Package != "giftcard" || s.Class != "GulimallGiftCardApplication" {
		t.Errorf("unexpected naming: %+v", s)
	}
}

func TestScaffoldCreatesTheServiceAndRegistersItEverywhere(t *testing.T) {
	root := tempRepo(t)
	valuesBefore := read(t, filepath.Join(root, valuesPath))
	s, _ := NewService("loyalty", 15900, true, "")
	changes, err := Scaffold(root, s, false)
	if err != nil {
		t.Fatal(err)
	}
	if len(changes) != 9 {
		t.Errorf("want 6 files + 3 registry edits, got %d: %v", len(changes), changes)
	}
	for _, f := range []string{"pom.xml", "Dockerfile", "src/main/resources/application.yml",
		"src/main/java/com/atguigu/gulimall/loyalty/GulimallLoyaltyApplication.java",
		"src/main/java/com/atguigu/gulimall/loyalty/controller/PingController.java",
		"src/test/java/com/atguigu/gulimall/loyalty/controller/PingControllerTest.java"} {
		if _, err := os.Stat(filepath.Join(root, "gulimall-loyalty", f)); err != nil {
			t.Errorf("missing %s", f)
		}
	}
	if d := read(t, filepath.Join(root, "gulimall-loyalty/Dockerfile")); !strings.Contains(d, "USER 1000:1000") || !strings.Contains(d, "EXPOSE 15900") {
		t.Errorf("Dockerfile should run unprivileged on port 15900:\n%s", d)
	}
	if !strings.Contains(read(t, filepath.Join(root, "gulimall-loyalty/src/main/resources/application.yml")), "enabled: true") {
		t.Error("--redis should keep the Redis health check on")
	}

	var v struct {
		Images map[string]struct{ Repository string } `yaml:"images"`
		Apps   map[string]struct {
			Name  string
			Port  int
			Redis bool
		} `yaml:"apps"`
		Otel map[string]any `yaml:"otel"`
	}
	values := read(t, filepath.Join(root, valuesPath))
	if err := yaml.Unmarshal([]byte(values), &v); err != nil {
		t.Fatal(err)
	}
	if v.Images["loyalty"].Repository != "gulimall-loyalty" || v.Apps["loyalty"].Port != 15900 || !v.Apps["loyalty"].Redis {
		t.Errorf("chart entry wrong: image %+v app %+v", v.Images["loyalty"], v.Apps["loyalty"])
	}
	if v.Otel == nil {
		t.Error("blocks after apps: must survive the edit")
	}
	for _, line := range strings.Split(valuesBefore, "\n") {
		if strings.HasPrefix(strings.TrimSpace(line), "#") && !strings.Contains(values, line) {
			t.Errorf("comment lost: %q", line)
		}
	}
	if !strings.Contains(read(t, filepath.Join(root, pomPath)), "<module>gulimall-loyalty</module>") {
		t.Error("pom.xml should list the module")
	}
	if n := strings.Count(read(t, filepath.Join(root, commonPath)), "\n  gulimall-loyalty\n"); n != 2 {
		t.Errorf("common.sh should list the module in both arrays, found %d", n)
	}
	if gaps := registrationGaps(root); len(gaps) != 0 {
		t.Errorf("a scaffolded service should be fully registered, gaps: %v", gaps)
	}
}

func TestRefusalsLeaveTheRepoUntouched(t *testing.T) {
	root := tempRepo(t)
	before := read(t, filepath.Join(root, valuesPath)) + read(t, filepath.Join(root, pomPath)) + read(t, filepath.Join(root, commonPath))
	for _, tc := range []struct {
		name string
		port int
		want string
	}{
		{"coupon", 15000, "gulimall-coupon already exists"},
		{"loyalty", 12000, "port 12000 is already used by coupon"},
	} {
		s, _ := NewService(tc.name, tc.port, false, "")
		if _, err := Scaffold(root, s, false); err == nil || !strings.Contains(err.Error(), tc.want) {
			t.Errorf("%s:%d: want error containing %q, got %v", tc.name, tc.port, tc.want, err)
		}
	}
	after := read(t, filepath.Join(root, valuesPath)) + read(t, filepath.Join(root, pomPath)) + read(t, filepath.Join(root, commonPath))
	if before != after {
		t.Error("a refused scaffold must not change any file")
	}
	if _, err := os.Stat(filepath.Join(root, "gulimall-loyalty")); err == nil {
		t.Error("a refused scaffold must not create the module")
	}
}

func TestDryRunWritesNothing(t *testing.T) {
	root := tempRepo(t)
	before := read(t, filepath.Join(root, valuesPath))
	s, _ := NewService("loyalty", 15900, false, "")
	changes, err := Scaffold(root, s, true)
	if err != nil || len(changes) != 9 {
		t.Fatalf("dry run: %v %v", changes, err)
	}
	if read(t, filepath.Join(root, valuesPath)) != before {
		t.Error("dry run changed values.yaml")
	}
	if _, err := os.Stat(filepath.Join(root, "gulimall-loyalty")); err == nil {
		t.Error("dry run created the module")
	}
}

func TestRegistrationGapsFindsManualDrift(t *testing.T) {
	root := tempRepo(t)
	os.Remove(filepath.Join(root, "gulimall-cart/Dockerfile"))
	pom := strings.Replace(read(t, filepath.Join(root, pomPath)), "<module>gulimall-ware</module>", "", 1)
	write(t, filepath.Join(root, pomPath), pom)
	gaps := strings.Join(registrationGaps(root), "; ")
	for _, want := range []string{"gulimall-cart: no Dockerfile", "gulimall-ware: not in pom.xml"} {
		if !strings.Contains(gaps, want) {
			t.Errorf("gaps %q should include %q", gaps, want)
		}
	}
}

// The generated chart entry must pass the same guardrails as everything else.
func TestScaffoldedServicePassesTheGuardrails(t *testing.T) {
	for _, tool := range []string{"helm", "conftest"} {
		if _, err := exec.LookPath(tool); err != nil {
			t.Skip(tool + " not installed")
		}
	}
	root := tempRepo(t)
	src, _ := filepath.Abs("../..")
	copyFile(t, filepath.Join(src, "platform/policy/workloads.rego"), filepath.Join(root, "platform/policy/workloads.rego"))
	s, _ := NewService("loyalty", 15900, false, "")
	if _, err := Scaffold(root, s, false); err != nil {
		t.Fatal(err)
	}
	manifest, err := renderChart(root, "kind")
	if err != nil || !strings.Contains(manifest, "name: gulimall-loyalty") {
		t.Fatalf("render: %v\n%s", err, manifest[:min(400, len(manifest))])
	}
	var out strings.Builder
	if err := Policy(root, &out); err != nil {
		t.Fatalf("guardrails failed for a scaffolded service:\n%s", out.String())
	}
}
