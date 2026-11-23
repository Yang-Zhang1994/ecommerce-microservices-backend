package main

import rego.v1

good := {
	"kind": "Deployment",
	"metadata": {"name": "svc"},
	"spec": {"template": {"metadata": {}, "spec": {
		"securityContext": {"runAsNonRoot": true},
		"containers": [{
			"name": "svc", "image": "gulimall-svc:abc123",
			"resources": {"requests": {"cpu": "100m", "memory": "256Mi"}, "limits": {"memory": "512Mi"}},
			"readinessProbe": {"httpGet": {"path": "/actuator/health"}},
			"livenessProbe": {"httpGet": {"path": "/actuator/health"}},
			"env": [{"name": "DB_PASSWORD", "valueFrom": {"secretKeyRef": {"name": "s", "key": "k"}}}],
		}],
	}}},
}

with_container(patch) := object.union(good, {"spec": {"template": {"spec": {"containers": [object.union(good.spec.template.spec.containers[0], patch)]}}}})

test_good_workload_passes if count(deny) == 0 with input as good

test_missing_resources_denied if {
	d := deny with input as json.remove(good, ["spec/template/spec/containers/0/resources"])
	count(d) == 3
}

test_missing_probes_denied if {
	bad := json.remove(good, ["spec/template/spec/containers/0/readinessProbe", "spec/template/spec/containers/0/livenessProbe"])
	d := deny with input as bad
	count(d) == 2
}

test_jobs_do_not_need_probes if {
	job := json.remove(object.union(good, {"kind": "Job"}), ["spec/template/spec/containers/0/readinessProbe", "spec/template/spec/containers/0/livenessProbe"])
	count(deny) == 0 with input as job
}

test_latest_and_untagged_images_denied if {
	count(deny) == 1 with input as with_container({"image": "redis:latest"})
	count(deny) == 1 with input as with_container({"image": "redis"})
}

test_root_denied_unless_annotated if {
	root := json.remove(good, ["spec/template/spec/securityContext"])
	d := deny with input as root
	some msg in d
	contains(msg, "may run as root")
	annotated := object.union(root, {"spec": {"template": {"metadata": {"annotations": {"policy.grainmart/allow-root": "upstream image needs uid 0"}}}}})
	count(deny) == 0 with input as annotated
}

test_container_level_non_root_is_enough if {
	root := json.remove(good, ["spec/template/spec/securityContext"])
	count(deny) == 0 with input as with_container_from(root, {"securityContext": {"runAsNonRoot": true}})
}

with_container_from(base, patch) := object.union(base, {"spec": {"template": {"spec": {"containers": [object.union(base.spec.template.spec.containers[0], patch)]}}}})

test_literal_secret_env_denied if {
	d := deny with input as with_container({"env": [{"name": "RABBITMQ_DEFAULT_PASS", "value": "guest"}]})
	count(d) == 1
}

test_non_secret_literal_env_allowed if {
	count(deny) == 0 with input as with_container({"env": [{"name": "SERVER_PORT", "value": "8080"}]})
	count(deny) == 0 with input as with_container({"env": [{"name": "BYPASS_CACHE", "value": "true"}]})
	count(deny) == 0 with input as with_container({"env": [{"name": "TOKENIZER_MODEL", "value": "x"}]})
}

test_services_are_ignored if count(deny) == 0 with input as {"kind": "Service", "metadata": {"name": "x"}, "spec": {}}
