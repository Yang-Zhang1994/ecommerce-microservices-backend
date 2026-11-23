package main

import (
	"embed"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"sort"
	"strings"
	"text/template"
)

//go:embed templates/service/*.tmpl
var serviceTemplates embed.FS

// Service is everything the templates and registry edits need to know about a new service.
type Service struct {
	Name        string // "review": used in URLs, Helm keys and image keys
	Module      string // "gulimall-review": Maven module, image repository, k8s name
	Package     string // "review": Java package segment
	Class       string // "GulimallReviewApplication"
	Port        int
	Redis       bool
	Description string
}

var validName = regexp.MustCompile(`^[a-z][a-z0-9]*(-[a-z0-9]+)*$`)

func NewService(name string, port int, redis bool, description string) (Service, error) {
	if !validName.MatchString(name) || len(name) > 20 {
		return Service{}, fmt.Errorf("service name %q must be lowercase letters, digits and single hyphens, at most 20 characters (e.g. review, gift-card)", name)
	}
	if strings.HasPrefix(name, "gulimall") {
		return Service{}, fmt.Errorf("leave out the gulimall- prefix: use %q", strings.TrimPrefix(strings.TrimPrefix(name, "gulimall"), "-"))
	}
	if port < 1024 || port > 65535 {
		return Service{}, fmt.Errorf("port %d must be between 1024 and 65535", port)
	}
	pascal := ""
	for _, part := range strings.Split(name, "-") {
		pascal += strings.ToUpper(part[:1]) + part[1:]
	}
	if description == "" {
		description = "GrainMart " + strings.ReplaceAll(name, "-", " ") + " service"
	}
	return Service{
		Name: name, Module: "gulimall-" + name, Package: strings.ReplaceAll(name, "-", ""),
		Class: "Gulimall" + pascal + "Application", Port: port, Redis: redis, Description: description,
	}, nil
}

// Files maps each generated file's path (relative to the repo root) to its content.
func (s Service) Files() (map[string]string, error) {
	javaDir := filepath.Join(s.Module, "src/main/java/com/atguigu/gulimall", s.Package)
	testDir := filepath.Join(s.Module, "src/test/java/com/atguigu/gulimall", s.Package)
	targets := map[string]string{
		"pom.xml.tmpl":                filepath.Join(s.Module, "pom.xml"),
		"Dockerfile.tmpl":             filepath.Join(s.Module, "Dockerfile"),
		"application.yml.tmpl":        filepath.Join(s.Module, "src/main/resources/application.yml"),
		"Application.java.tmpl":       filepath.Join(javaDir, s.Class+".java"),
		"PingController.java.tmpl":    filepath.Join(javaDir, "controller/PingController.java"),
		"PingControllerTest.java.tmpl": filepath.Join(testDir, "controller/PingControllerTest.java"),
	}
	out := map[string]string{}
	for tmplName, path := range targets {
		t, err := template.ParseFS(serviceTemplates, "templates/service/"+tmplName)
		if err != nil {
			return nil, err
		}
		var b strings.Builder
		if err := t.Execute(&b, s); err != nil {
			return nil, err
		}
		out[path] = b.String()
	}
	return out, nil
}

// Scaffold writes the module and registers it everywhere a service has to be listed. It checks every
// edit before writing anything, so a refusal (name or port taken) leaves the repo untouched.
func Scaffold(root string, s Service, dryRun bool) ([]string, error) {
	if _, err := os.Stat(filepath.Join(root, s.Module)); err == nil {
		return nil, fmt.Errorf("%s already exists", s.Module)
	}
	files, err := s.Files()
	if err != nil {
		return nil, err
	}
	edits, err := PlanRegistryEdits(root, s)
	if err != nil {
		return nil, err
	}
	var changed []string
	for path := range files {
		changed = append(changed, "create "+path)
	}
	sort.Strings(changed)
	for _, e := range edits {
		changed = append(changed, "update "+e.Path+" ("+e.What+")")
	}
	if dryRun {
		return changed, nil
	}
	for path, content := range files {
		full := filepath.Join(root, path)
		if err := os.MkdirAll(filepath.Dir(full), 0o755); err != nil {
			return nil, err
		}
		if err := os.WriteFile(full, []byte(content), 0o644); err != nil {
			return nil, err
		}
	}
	for _, e := range edits {
		if err := os.WriteFile(filepath.Join(root, e.Path), []byte(e.Content), 0o644); err != nil {
			return nil, err
		}
	}
	return changed, nil
}
