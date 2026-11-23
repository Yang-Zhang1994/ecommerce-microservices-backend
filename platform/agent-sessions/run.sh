#!/usr/bin/env bash
# Run one headless Claude Code triage session against the read-only platform MCP server.
# Usage: platform/agent-sessions/run.sh <fault-name>   (writes platform/agent-sessions/<fault>.jsonl)
set -euo pipefail
cd "$(dirname "$0")/../.."
claude -p "$(cat platform/agent-sessions/prompt.md)" \
  --mcp-config platform/mcp/mcp.json --strict-mcp-config \
  --allowedTools "mcp__platform__list_pods,mcp__platform__list_services,mcp__platform__release_drift,mcp__platform__recent_events,mcp__platform__describe_pod,mcp__platform__pod_logs,mcp__platform__workload_config,mcp__platform__release_history,mcp__platform__search_runbooks" \
  --output-format stream-json --verbose < /dev/null > "platform/agent-sessions/$1.jsonl"
