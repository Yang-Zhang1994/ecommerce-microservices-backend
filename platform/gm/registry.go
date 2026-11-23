package main

import (
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"sort"
	"strings"

	"gopkg.in/yaml.v3"
)

const (
	pomPath    = "pom.xml"
	valuesPath = "k8s/helm/gulimall/values.yaml"
	commonPath = "k8s/scripts/common.sh"
)

// Edit is a whole-file replacement, computed up front so nothing is written if any edit fails.
type Edit struct {
	Path, What, Content string
}

type chartValues struct {
	Images map[string]any `yaml:"images"`
	Apps   map[string]struct {
		Port int `yaml:"port"`
	} `yaml:"apps"`
}

func PlanRegistryEdits(root string, s Service) ([]Edit, error) {
	read := func(p string) (string, error) {
		b, err := os.ReadFile(filepath.Join(root, p))
		return string(b), err
	}
	pom, err := read(pomPath)
	if err != nil {
		return nil, err
	}
	values, err := read(valuesPath)
	if err != nil {
		return nil, err
	}
	common, err := read(commonPath)
	if err != nil {
		return nil, err
	}

	var v chartValues
	if err := yaml.Unmarshal([]byte(values), &v); err != nil {
		return nil, fmt.Errorf("%s: %w", valuesPath, err)
	}
	if _, taken := v.Apps[s.Name]; taken {
		return nil, fmt.Errorf("%s already has an app called %q", valuesPath, s.Name)
	}
	for key, app := range v.Apps {
		if app.Port == s.Port {
			return nil, fmt.Errorf("port %d is already used by %s; pick another (free ports: %s)", s.Port, key, freePortsHint(v))
		}
	}

	newPom, err := addPomModule(pom, s.Module)
	if err != nil {
		return nil, err
	}
	image := fmt.Sprintf("  %s:\n    repository: %s\n    tag: local\n", s.Name, s.Module)
	newValues, err := appendToBlock(values, "images", image)
	if err != nil {
		return nil, err
	}
	app := fmt.Sprintf("  %s:\n    enabled: true\n    name: %s\n    imageKey: %s\n    replicas: 1\n    port: %d\n    consul: true\n",
		s.Name, s.Module, s.Name, s.Port)
	if s.Redis {
		app += "    redis: true\n"
	}
	app += "    probeDelay: 45\n"
	if newValues, err = appendToBlock(newValues, "apps", app); err != nil {
		return nil, err
	}
	if err := yaml.Unmarshal([]byte(newValues), &v); err != nil || v.Apps[s.Name].Port != s.Port || v.Images[s.Name] == nil {
		return nil, fmt.Errorf("internal error: edited %s does not parse back to the new service", valuesPath)
	}
	newCommon := common
	for _, array := range []string{"JAVA_MODULES", "DOCKER_IMAGES"} {
		if newCommon, err = addToBashArray(newCommon, array, s.Module); err != nil {
			return nil, err
		}
	}
	return []Edit{
		{pomPath, "Maven module", newPom},
		{valuesPath, "Helm image and app entry", newValues},
		{commonPath, "kind build lists", newCommon},
	}, nil
}

func addPomModule(pom, module string) (string, error) {
	if strings.Contains(pom, "<module>"+module+"</module>") {
		return "", fmt.Errorf("%s already lists %s", pomPath, module)
	}
	i := strings.LastIndex(pom, "</module>")
	if i < 0 {
		return "", fmt.Errorf("%s has no <module> entries", pomPath)
	}
	lineStart := strings.LastIndex(pom[:i], "\n") + 1
	indent := pom[lineStart : lineStart+len(pom[lineStart:])-len(strings.TrimLeft(pom[lineStart:], " \t"))]
	end := i + len("</module>")
	return pom[:end] + "\n" + indent + "<module>" + module + "</module>" + pom[end:], nil
}

// appendToBlock inserts text at the end of a top-level YAML mapping (before the next top-level key),
// keeping every comment and blank line in the file as it was.
func appendToBlock(doc, key, text string) (string, error) {
	lines := strings.SplitAfter(doc, "\n")
	start := -1
	for i, l := range lines {
		if strings.TrimRight(l, "\r\n") == key+":" {
			start = i
			break
		}
	}
	if start < 0 {
		return "", fmt.Errorf("no top-level %q block in %s", key, valuesPath)
	}
	end := len(lines)
	for i := start + 1; i < len(lines); i++ {
		if l := lines[i]; l != "" && l[0] != ' ' && l[0] != '\n' && l[0] != '#' {
			end = i
			break
		}
	}
	// Step back over trailing blank lines so the new entry sits with its block.
	for end > start+1 && strings.TrimSpace(lines[end-1]) == "" {
		end--
	}
	if !strings.HasSuffix(lines[end-1], "\n") {
		lines[end-1] += "\n"
	}
	return strings.Join(lines[:end], "") + text + strings.Join(lines[end:], ""), nil
}

func addToBashArray(script, array, item string) (string, error) {
	re := regexp.MustCompile(`(?s)` + array + `=\(\n(.*?)\n\)`)
	m := re.FindStringSubmatchIndex(script)
	if m == nil {
		return "", fmt.Errorf("%s has no %s=( ... ) array", commonPath, array)
	}
	body := script[m[2]:m[3]]
	if regexp.MustCompile(`(?m)^\s*` + regexp.QuoteMeta(item) + `\s*$`).MatchString(body) {
		return "", fmt.Errorf("%s already lists %s in %s", commonPath, item, array)
	}
	return script[:m[3]] + "\n  " + item + script[m[3]:], nil
}

func freePortsHint(v chartValues) string {
	used := map[int]bool{}
	for _, a := range v.Apps {
		used[a.Port] = true
	}
	var free []string
	for p := 15000; len(free) < 3 && p < 16000; p += 100 {
		if !used[p] {
			free = append(free, fmt.Sprint(p))
		}
	}
	sort.Strings(free)
	return strings.Join(free, ", ")
}
