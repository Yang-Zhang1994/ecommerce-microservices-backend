---
name: consul-host-typo
description: A config change outside Helm points the review service at a Consul host that doesn't exist
runs: 3
max_turns: 25
timeout_seconds: 600
allowed_tools: [Skill, mcp__plugin_grainmart-triage_platform__list_pods, mcp__plugin_grainmart-triage_platform__recent_events, mcp__plugin_grainmart-triage_platform__describe_pod, mcp__plugin_grainmart-triage_platform__pod_logs, mcp__plugin_grainmart-triage_platform__workload_config, mcp__plugin_grainmart-triage_platform__list_services, mcp__plugin_grainmart-triage_platform__release_drift, mcp__plugin_grainmart-triage_platform__release_history, mcp__plugin_grainmart-triage_platform__search_runbooks]
---
gulimall-review in the gm-sandbox namespace keeps restarting. What's the root cause, and what should we do about it?
