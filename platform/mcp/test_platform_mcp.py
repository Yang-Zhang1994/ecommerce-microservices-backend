import asyncio
import json

import pytest
from mcp import Client

from platform_mcp import Cluster, Refused, build, redact, redact_json

PODS = {"items": [{
    "metadata": {"name": "gulimall-review-abc", "labels": {"app": "gulimall-review"}},
    "spec": {"containers": [{"image": "gulimall-review:sbx-1"}]},
    "status": {"phase": "Running", "containerStatuses": [{
        "ready": False, "restartCount": 4, "image": "gulimall-review:sbx-1",
        "state": {"waiting": {"reason": "CrashLoopBackOff", "message": "back-off 40s"}},
        "lastState": {"terminated": {"reason": "Error", "exitCode": 1}}}]}}]}


# A workload that reads a password from a Secret and (wrongly) sets another one literally.
DEPLOYMENT = {"spec": {"replicas": 1, "template": {"spec": {"containers": [{
    "image": "gulimall-review:sbx-1", "imagePullPolicy": "Never",
    "env": [{"name": "REVIEW_DB_PASSWORD", "valueFrom": {"secretKeyRef": {"name": "review-db", "key": "password"}}},
            {"name": "API_TOKEN", "value": "s3cr3t"}, {"name": "SERVER_PORT", "value": "15000"}]}]}}},
    "status": {}}


# What Helm deployed: the live DEPLOYMENT above has since gained a secret reference and a literal token.
MANIFEST = """---
kind: Service
metadata: {name: gulimall-review}
---
kind: Deployment
metadata: {name: gulimall-review}
spec:
  template:
    spec:
      containers:
        - image: gulimall-review:sbx-1
          env:
            - {name: SERVER_PORT, value: "15000"}
"""


class FakeRunner:
    def __init__(self):
        self.calls = []

    def __call__(self, cmd):
        self.calls.append(cmd)
        if cmd[-2:] == ["-o", "json"] and "pods" in cmd:
            return json.dumps(PODS, indent=2)
        if cmd[-2:] == ["-o", "json"] and "deployment" in cmd:
            return json.dumps(DEPLOYMENT, indent=2)
        if cmd[-2:] == ["-o", "json"] and "services" in cmd:
            return json.dumps({"items": [{"metadata": {"name": "consul"}, "spec": {"type": "ClusterIP",
                              "ports": [{"port": 8500}], "selector": {"app": "consul"}}}]})
        if cmd[-3:-1] == ["get", "manifest"]:
            return MANIFEST
        if "logs" in cmd:
            return "Caused by: java.net.UnknownHostException: consul-typo\nDB_PASSWORD=hunter2\n"
        return "ok"


@pytest.fixture
def cluster(tmp_path):
    return Cluster(namespace="gm-sandbox", runner=FakeRunner(), audit_path=tmp_path / "audit.log")


def call(cluster, tool, args=None):
    async def go():
        async with Client(build(cluster)) as client:
            return await client.call_tool(tool, args or {})
    return asyncio.run(go())


@pytest.mark.parametrize("args", [["delete", "pod", "x"], ["apply", "-f", "x.yaml"], ["exec", "x", "--", "sh"],
                                  ["patch", "deployment", "x"], ["scale", "deployment", "x", "--replicas=0"], []])
def test_write_verbs_are_refused(cluster, args):
    with pytest.raises(Refused):
        cluster.run("kubectl", args)
    assert cluster.runner.calls == []


@pytest.mark.parametrize("args", [["get", "pods", "-A"], ["get", "pods", "-n", "kube-system"],
                                  ["get", "pods", "--namespace=gulimall"], ["get", "pods", "--context=prod"],
                                  ["get", "secrets"], ["get", "secret", "db", "-o", "yaml"]])
def test_namespace_context_and_secrets_are_pinned(cluster, args):
    with pytest.raises(Refused):
        cluster.run("kubectl", args)


def test_helm_is_read_only_too(cluster):
    for bad in (["upgrade", "x", "."], ["rollback", "x", "1"], ["uninstall", "x"], ["get", "values", "x"]):
        with pytest.raises(Refused):
            cluster.run("helm", bad)
    cluster.run("helm", ["history", "gm-sandbox"])
    assert cluster.runner.calls[-1][:5] == ["helm", "--kube-context", "kind-gulimall", "-n", "gm-sandbox"]


def test_reads_are_pinned_and_audited(cluster):
    cluster.run("kubectl", ["get", "pods"])
    assert cluster.runner.calls[-1] == ["kubectl", "--context", "kind-gulimall", "-n", "gm-sandbox", "get", "pods"]
    line = json.loads(cluster.audit_path.read_text().splitlines()[-1])
    assert line["cmd"][-2:] == ["get", "pods"]


def test_credentials_are_masked():
    assert redact("DB_PASSWORD=hunter2 token: abc API_KEY=xyz port=8080") == \
        "DB_PASSWORD=[redacted] token: [redacted] API_KEY=[redacted] port=8080"


def test_list_pods_summarizes_why_a_pod_is_down(cluster):
    out = call(cluster, "list_pods").structured_content["result"][0]
    assert out["waiting_reason"] == "CrashLoopBackOff" and out["restarts"] == 4
    assert out["last_exit"] == {"reason": "Error", "exit_code": 1}


def test_previous_logs_come_back_masked(cluster):
    text = call(cluster, "pod_logs", {"pod": "gulimall-review-abc", "previous": True, "tail": 5000}).content[0].text
    assert "UnknownHostException: consul-typo" in text and "hunter2" not in text
    assert "--previous" in cluster.runner.calls[-1] and "--tail=300" in cluster.runner.calls[-1]


def test_tools_offered_to_the_agent(cluster):
    async def go():
        async with Client(build(cluster)) as client:
            return sorted(t.name for t in (await client.list_tools()).tools)
    assert asyncio.run(go()) == ["describe_pod", "list_pods", "list_services", "pod_logs", "recent_events",
                                 "release_drift", "release_history", "search_runbooks", "workload_config"]


def test_runbook_search_finds_the_matching_runbook(cluster):
    hits = call(cluster, "search_runbooks", {"query": "CreateContainerConfigError secret"}).structured_content["result"]
    assert hits[0]["runbook"] == "pod-not-starting.md"


def test_secret_references_survive_and_literal_secrets_are_masked(cluster):
    # Regression: masking raw JSON text matched `"key": "password"` and broke parsing.
    out = call(cluster, "workload_config", {"name": "gulimall-review"}).structured_content
    env = {e["name"]: e for e in out["env"]}
    assert env["REVIEW_DB_PASSWORD"]["valueFrom"]["secretKeyRef"] == {"name": "review-db", "key": "password"}
    assert env["API_TOKEN"]["value"] == "[redacted]" and env["SERVER_PORT"]["value"] == "15000"
    assert out["available"] == 0


def test_redact_json_masks_strings_inside_structures():
    assert redact_json({"msg": "login password=hunter2 failed", "n": 3}) == {"msg": "login password=[redacted] failed", "n": 3}


def test_release_drift_reports_changes_made_outside_helm(cluster):
    drift = call(cluster, "release_drift").structured_content["result"]
    fields = {d["field"]: d for d in drift}
    assert set(fields) == {"env API_TOKEN", "env REVIEW_DB_PASSWORD"}
    assert fields["env API_TOKEN"] == {"deployment": "gulimall-review", "field": "env API_TOKEN",
                                        "release": "(not set)", "live": "[redacted]"}
    assert fields["env REVIEW_DB_PASSWORD"]["live"] == {"secretKeyRef": {"name": "review-db", "key": "password"}}


def test_list_services_gives_dns_names_and_ports(cluster):
    assert call(cluster, "list_services").structured_content["result"] == [
        {"service": "consul", "type": "ClusterIP", "ports": [8500], "selector": {"app": "consul"}}]
