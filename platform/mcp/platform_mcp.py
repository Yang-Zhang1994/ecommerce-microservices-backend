"""Read-only MCP server for triaging GrainMart workloads on Kubernetes.

An agent (Claude Code, or any MCP client) can look at pods, events, logs, workload config, Helm history
and the runbooks in one namespace. It cannot change anything: every kubectl/helm call goes through
`Cluster.run`, which only allows read verbs, pins the context and namespace, and writes an audit line.

Run:  PLATFORM_NAMESPACE=gm-sandbox python platform_mcp.py
"""

from __future__ import annotations

import json
import os
import re
import subprocess
import time
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Callable

import yaml
from mcp.server.mcpserver import MCPServer

ROOT = Path(__file__).resolve().parents[2]
RUNBOOKS = ROOT / "platform" / "runbooks"

READ_VERBS = {"get", "describe", "logs"}
HELM_READ = {"history", "status", "get"}
# Values that look like credentials are masked in anything returned to the agent.
SECRET_PATTERN = re.compile(r"(?i)((?:password|passwd|secret|token|api[_-]?key)\S*\s*[=:]\s*)\S+")
MAX_LOG_LINES = 300


class Refused(ValueError):
    """The agent asked for something outside the read-only allowlist."""


SECRET_NAME = re.compile(r"(?i)(^|_)(PASS|PASSWD|PASSWORD|SECRET|TOKEN|API_?KEY|PRIVATE_KEY)($|_)")


def redact(text: str) -> str:
    return SECRET_PATTERN.sub(r"\1[redacted]", text)


def redact_json(obj: Any) -> Any:
    """Mask credentials inside parsed JSON (text masking would corrupt the JSON itself): literal env
    values with secret-like names, and anything in a string that looks like `password=...`."""
    if isinstance(obj, dict):
        out = {k: redact_json(v) for k, v in obj.items()}
        if isinstance(obj.get("name"), str) and "value" in obj and SECRET_NAME.search(obj["name"]):
            out["value"] = "[redacted]"
        return out
    if isinstance(obj, list):
        return [redact_json(v) for v in obj]
    return redact(obj) if isinstance(obj, str) else obj


@dataclass
class Cluster:
    namespace: str
    context: str = "kind-gulimall"
    audit_path: Path | None = None
    runner: Callable[[list[str]], str] = field(default=lambda cmd: subprocess.run(
        cmd, capture_output=True, text=True, timeout=30, check=False).stdout)

    def run_json(self, args: list[str]) -> dict[str, Any]:
        """A read whose output is parsed, then masked (masking the raw text would break the JSON)."""
        raw = self.run("kubectl", [*args, "-o", "json"], mask=False)
        return redact_json(json.loads(raw)) if raw.strip() else {}

    def run(self, tool: str, args: list[str], mask: bool = True) -> str:
        if tool == "kubectl":
            if not args or args[0] not in READ_VERBS:
                raise Refused(f"kubectl {args[:1]} is not allowed; this server is read-only ({sorted(READ_VERBS)})")
            if args[0] == "get" and any(a.startswith("secret") for a in args[1:2]):
                raise Refused("reading Secrets is not allowed; ask a human to check the secret exists")
            if any(a in ("-A", "--all-namespaces") or a.startswith(("-n", "--namespace", "--context")) for a in args):
                raise Refused("namespace and context are fixed by the server")
            cmd = ["kubectl", "--context", self.context, "-n", self.namespace, *args]
        elif tool == "helm":
            if not args or args[0] not in HELM_READ or (args[0] == "get" and args[1:2] != ["manifest"]):
                raise Refused(f"helm {args[:2]} is not allowed; only history, status and get manifest")
            cmd = ["helm", "--kube-context", self.context, "-n", self.namespace, *args]
        else:
            raise Refused(f"{tool} is not allowed")
        if self.audit_path:
            with self.audit_path.open("a") as f:
                f.write(json.dumps({"ts": round(time.time(), 3), "cmd": cmd}) + "\n")
        out = self.runner(cmd)
        return redact(out) if mask else out


def summarize_pods(raw: dict[str, Any]) -> list[dict[str, Any]]:
    pods = []
    for item in raw.get("items", []):
        statuses = item.get("status", {}).get("containerStatuses", [])
        c = statuses[0] if statuses else {}
        waiting = c.get("state", {}).get("waiting", {})
        last = c.get("lastState", {}).get("terminated", {})
        pods.append({
            "pod": item["metadata"]["name"],
            "app": item["metadata"].get("labels", {}).get("app"),
            "phase": item.get("status", {}).get("phase"),
            "ready": c.get("ready", False),
            "restarts": c.get("restartCount", 0),
            "waiting_reason": waiting.get("reason"),
            "waiting_message": (waiting.get("message") or "")[:300] or None,
            "last_exit": {"reason": last.get("reason"), "exit_code": last.get("exitCode")} if last else None,
            "image": c.get("image") or item["spec"]["containers"][0]["image"],
        })
    return pods


def build(cluster: Cluster) -> MCPServer:
    server = MCPServer("grainmart-platform")

    @server.tool()
    def list_pods() -> list[dict[str, Any]]:
        """Every pod in the namespace with readiness, restarts, why a container is waiting (e.g.
        CrashLoopBackOff, ErrImageNeverPull, CreateContainerConfigError) and its last exit."""
        return summarize_pods(cluster.run_json(["get", "pods"]))

    @server.tool()
    def recent_events(only_warnings: bool = True) -> str:
        """Namespace events, newest last. Warnings usually name the failing object and the reason."""
        args = ["get", "events", "--sort-by=.lastTimestamp"]
        if only_warnings:
            args.append("--field-selector=type=Warning")
        return cluster.run("kubectl", args)[-6000:]

    @server.tool()
    def describe_pod(pod: str) -> str:
        """`kubectl describe pod` output: container state, env sources, probes and the pod's events."""
        return cluster.run("kubectl", ["describe", "pod", pod])[-8000:]

    @server.tool()
    def pod_logs(pod: str, previous: bool = False, tail: int = 120) -> str:
        """Container logs (credentials masked). previous=true shows the run before the last restart,
        which is where a crash's stack trace is."""
        args = ["logs", pod, f"--tail={max(1, min(tail, MAX_LOG_LINES))}"]
        if previous:
            args.append("--previous")
        return cluster.run("kubectl", args) or "(no logs)"

    @server.tool()
    def workload_config(name: str) -> dict[str, Any]:
        """The Deployment's container spec: image, env (secret values never shown, only where they
        come from), probes and resources."""
        raw = cluster.run_json(["get", "deployment", name])
        if not raw:
            return {"error": f"no deployment {name!r}"}
        c = raw["spec"]["template"]["spec"]["containers"][0]
        return {"image": c.get("image"), "imagePullPolicy": c.get("imagePullPolicy"), "env": c.get("env", []),
                "readinessProbe": c.get("readinessProbe"), "resources": c.get("resources"),
                "replicas": raw["spec"].get("replicas"), "available": raw.get("status", {}).get("availableReplicas", 0)}

    @server.tool()
    def list_services() -> list[dict[str, Any]]:
        """Services in the namespace: the DNS names other workloads should use, and their ports."""
        raw = cluster.run_json(["get", "services"])
        return [{"service": i["metadata"]["name"], "type": i["spec"].get("type"),
                 "ports": [p.get("port") for p in i["spec"].get("ports", [])],
                 "selector": i["spec"].get("selector")} for i in raw.get("items", [])]

    @server.tool()
    def release_drift(release: str = "gm-sandbox") -> list[dict[str, Any]]:
        """Compare each Deployment's live image and env with what the Helm release deployed. A difference
        means someone changed the cluster outside Helm (kubectl edit/patch/set env), and the next helm
        upgrade may overwrite or keep it unexpectedly."""
        manifest = cluster.run("helm", ["get", "manifest", release], mask=False)
        drift = []
        for doc in yaml.safe_load_all(manifest):
            if not doc or doc.get("kind") != "Deployment":
                continue
            name = doc["metadata"]["name"]
            live = cluster.run_json(["get", "deployment", name])
            if not live:
                drift.append({"deployment": name, "change": "in the release but missing from the cluster"})
                continue
            want = doc["spec"]["template"]["spec"]["containers"][0]
            have = live["spec"]["template"]["spec"]["containers"][0]
            if want.get("image") != have.get("image"):
                drift.append({"deployment": name, "field": "image", "release": want.get("image"), "live": have.get("image")})
            want_env = {e["name"]: e.get("value", e.get("valueFrom")) for e in redact_json(want.get("env", []))}
            have_env = {e["name"]: e.get("value", e.get("valueFrom")) for e in have.get("env", [])}
            for key in sorted(set(want_env) | set(have_env)):
                if want_env.get(key) != have_env.get(key):
                    drift.append({"deployment": name, "field": f"env {key}",
                                  "release": want_env.get(key, "(not set)"), "live": have_env.get(key, "(not set)")})
        return drift

    @server.tool()
    def release_history(release: str = "gm-sandbox") -> str:
        """Helm revisions for the release: what was deployed when, and whether it succeeded."""
        return cluster.run("helm", ["history", release, "--max", "10"])

    @server.tool()
    def search_runbooks(query: str) -> list[dict[str, str]]:
        """Search the team's runbooks (platform/runbooks) for a symptom or error string."""
        words = [w.lower() for w in re.findall(r"\w+", query) if len(w) > 2]
        hits = []
        for path in sorted(RUNBOOKS.glob("*.md")):
            text = path.read_text()
            score = sum(text.lower().count(w) for w in words)
            if score:
                hits.append((score, {"runbook": path.name, "content": text}))
        return [h for _, h in sorted(hits, key=lambda x: -x[0])[:2]]

    return server


def main() -> None:
    ns = os.environ.get("PLATFORM_NAMESPACE", "gm-sandbox")
    audit = Path(os.environ.get("PLATFORM_AUDIT_LOG", Path(__file__).with_name("audit.log")))
    build(Cluster(namespace=ns, audit_path=audit)).run()


if __name__ == "__main__":
    main()
