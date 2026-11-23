# Guardrails every GrainMart workload must meet before it reaches a cluster.
# Run: helm template ... | conftest test --policy platform/policy -
package main

import rego.v1

workload_kinds := {"Deployment", "StatefulSet", "DaemonSet", "Job"}

containers contains c if {
	workload_kinds[input.kind]
	some c in input.spec.template.spec.containers
}

name := sprintf("%s/%s", [input.kind, input.metadata.name])

# A pod may run as root only with a written reason, e.g. a third-party image that needs it.
allow_root if input.spec.template.metadata.annotations["policy.grainmart/allow-root"] != ""

pod_non_root if input.spec.template.spec.securityContext.runAsNonRoot == true

container_non_root(c) if c.securityContext.runAsNonRoot == true

deny contains msg if {
	some c in containers
	not c.resources.requests.cpu
	msg := sprintf("%s: container %q has no CPU request (the scheduler and HPA need one)", [name, c.name])
}

deny contains msg if {
	some c in containers
	not c.resources.requests.memory
	msg := sprintf("%s: container %q has no memory request", [name, c.name])
}

deny contains msg if {
	some c in containers
	not c.resources.limits.memory
	msg := sprintf("%s: container %q has no memory limit (one leak can starve the node)", [name, c.name])
}

deny contains msg if {
	input.kind != "Job"
	some c in containers
	not c.readinessProbe
	msg := sprintf("%s: container %q has no readiness probe (traffic arrives before it is ready)", [name, c.name])
}

deny contains msg if {
	input.kind != "Job"
	some c in containers
	not c.livenessProbe
	msg := sprintf("%s: container %q has no liveness probe (a hung process is never restarted)", [name, c.name])
}

deny contains msg if {
	some c in containers
	not contains(c.image, ":")
	msg := sprintf("%s: image %q has no tag", [name, c.image])
}

deny contains msg if {
	some c in containers
	endswith(c.image, ":latest")
	msg := sprintf("%s: image %q uses :latest, so a restart can silently change the code", [name, c.image])
}

deny contains msg if {
	some c in containers
	not allow_root
	not pod_non_root
	not container_non_root(c)
	msg := sprintf("%s: container %q may run as root; set runAsNonRoot or annotate policy.grainmart/allow-root with a reason", [name, c.name])
}

secret_like(env_name) if regex.match(`(?i)(^|_)(PASS|PASSWD|PASSWORD|SECRET|TOKEN|API_?KEY|PRIVATE_KEY)($|_)`, env_name)

deny contains msg if {
	some c in containers
	some e in c.env
	secret_like(e.name)
	e.value
	msg := sprintf("%s: env %q in container %q is a literal; read it from a Secret with valueFrom", [name, e.name, c.name])
}
