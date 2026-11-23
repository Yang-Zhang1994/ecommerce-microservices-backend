---
description: Find the root cause of an unhealthy GrainMart workload on Kubernetes using the read-only platform tools, and propose a fix for a human to apply. Use when a service is down, crash-looping, not ready, or stuck pending in the cluster.
argument-hint: "[service or symptom]"
allowed-tools: mcp__plugin_grainmart-triage_platform__list_pods mcp__plugin_grainmart-triage_platform__recent_events mcp__plugin_grainmart-triage_platform__describe_pod mcp__plugin_grainmart-triage_platform__pod_logs mcp__plugin_grainmart-triage_platform__workload_config mcp__plugin_grainmart-triage_platform__list_services mcp__plugin_grainmart-triage_platform__release_drift mcp__plugin_grainmart-triage_platform__release_history mcp__plugin_grainmart-triage_platform__search_runbooks
---

# Triage a GrainMart workload

The platform tools are read-only, pinned to one namespace, and audited. You cannot change the cluster;
your job is to find the root cause and hand a human an exact fix.

Work in this order, and stop gathering once the evidence names one cause:

1. `list_pods`: which pod is unhealthy, its waiting reason, restarts and last exit.
2. `recent_events`: the warning that names the failing object.
3. Branch on what you saw:
   - **Waiting, never started** (`ErrImageNeverPull`, `ImagePullBackOff`, `CreateContainerConfigError`):
     `describe_pod` shows the missing image, Secret or ConfigMap.
   - **Started, then exited** (`CrashLoopBackOff`, exit code 1): `pod_logs` with `previous=true`, then
     read the first `Caused by:`. A host name in the error? Check `workload_config` env against
     `list_services`.
   - **OOMKilled**: compare `workload_config` resources with the JVM heap in `JAVA_OPTS`.
4. `release_drift`: was the cluster changed outside Helm? If yes, say so; it decides whether the fix
   is a re-sync from Helm or a chart change.
5. `release_history`: which revision introduced it, if a Helm change did.
6. `search_runbooks` with the exact error string, and follow the matching runbook.

Answer in this shape:

- **Root cause:** one sentence.
- **Evidence:** the tool outputs that show it, quoted briefly.
- **Fix for a human:** exact commands or the chart value to change, and how to confirm it worked.
- **Not confirmed:** anything you inferred but couldn't see.

Never guess a value you didn't see in a tool result, and never claim you ran a command.
