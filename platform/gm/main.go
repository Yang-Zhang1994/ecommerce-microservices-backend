// gm is GrainMart's platform CLI: a golden path for new services, preflight checks, a sandbox deploy
// on kind, and the workload guardrails, so adding a service is one command instead of six hand edits.
package main

import (
	"errors"
	"flag"
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

const usage = `gm - GrainMart platform CLI

  gm new service NAME [--port N] [--redis] [--description TEXT] [--dry-run]
      Create gulimall-NAME (Spring Boot, Dockerfile, health checks, a test) and register it in
      pom.xml, the Helm chart and the kind build lists.
  gm doctor
      Check tools, the cluster, the chart and that every service is registered everywhere.
  gm policy
      Render the chart for kind and EKS and run the workload guardrails (platform/policy).
  gm sandbox deploy NAME
      Build NAME, load it into kind and deploy it alone into the gm-sandbox namespace.
  gm sandbox delete
      Remove the gm-sandbox namespace.
`

func main() {
	if err := run(os.Args[1:]); err != nil {
		fmt.Fprintln(os.Stderr, "gm:", err)
		os.Exit(1)
	}
}

func run(args []string) error {
	root, err := repoRoot()
	if err != nil {
		return err
	}
	if len(args) == 0 {
		fmt.Print(usage)
		return nil
	}
	switch strings.Join(args[:min(2, len(args))], " ") {
	case "new service":
		return cmdNewService(root, args[2:])
	case "sandbox deploy":
		if len(args) < 3 {
			return errors.New("usage: gm sandbox deploy NAME")
		}
		return SandboxDeploy(root, args[2])
	case "sandbox delete":
		return SandboxDelete()
	}
	switch args[0] {
	case "doctor":
		if !Doctor(root, os.Stdout) {
			return errors.New("some checks failed; see the fixes above")
		}
		return nil
	case "policy":
		return Policy(root, os.Stdout)
	case "help", "-h", "--help":
		fmt.Print(usage)
		return nil
	}
	return fmt.Errorf("unknown command %q\n\n%s", strings.Join(args, " "), usage)
}

func cmdNewService(root string, args []string) error {
	fs := flag.NewFlagSet("new service", flag.ContinueOnError)
	port := fs.Int("port", 15000, "HTTP port (must be unused by other apps)")
	redis := fs.Bool("redis", false, "wire Redis and include it in the health check")
	desc := fs.String("description", "", "one-line description")
	dry := fs.Bool("dry-run", false, "print what would change without writing")
	if len(args) == 0 {
		return errors.New("usage: gm new service NAME [--port N] [--redis] [--description TEXT] [--dry-run]")
	}
	name := args[0]
	if err := fs.Parse(args[1:]); err != nil {
		return err
	}
	s, err := NewService(name, *port, *redis, *desc)
	if err != nil {
		return err
	}
	changes, err := Scaffold(root, s, *dry)
	if err != nil {
		return err
	}
	verb := "Created"
	if *dry {
		verb = "Would make"
	}
	fmt.Printf("%s %d changes for %s:\n", verb, len(changes), s.Module)
	for _, c := range changes {
		fmt.Println("  " + c)
	}
	if !*dry {
		fmt.Printf("\nNext: gm policy && gm sandbox deploy %s\n", s.Name)
	}
	return nil
}

// repoRoot walks up from the working directory to the folder holding the root pom.xml and the chart.
func repoRoot() (string, error) {
	dir, err := os.Getwd()
	if err != nil {
		return "", err
	}
	for {
		if _, err := os.Stat(filepath.Join(dir, valuesPath)); err == nil {
			return dir, nil
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			return "", errors.New("run gm inside the GrainMart repository (no k8s/helm/gulimall/values.yaml found above here)")
		}
		dir = parent
	}
}
