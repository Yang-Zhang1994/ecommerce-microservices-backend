# grainmart-triage (Claude Code plugin)

Read-only incident triage for GrainMart on Kubernetes, packaged so any engineer can install it.

- **MCP server** (`server/platform_mcp.py`): nine tools over one namespace: pods with waiting reasons,
  events, describe, previous-run logs, workload config, Services, Helm history, **release drift** (live
  vs what Helm deployed) and runbook search.
  - **Read-only:** only `get`, `describe` and `logs`, plus helm `history`, `status` and `get manifest`.
    Secrets can't be read, and namespace and context are pinned.
  - **Audited and masked:** every call is audit-logged, and credentials are masked.
- **`/grainmart-triage:triage` skill**: the order to investigate in and the answer format (root cause,
  evidence, fix for a human, what wasn't confirmed).
- **Runbooks** (`runbooks/`): what each waiting reason or crash usually means.

## Install

```bash
claude plugin marketplace add Yang-Zhang1994/ecommerce-microservices-backend
claude plugin install grainmart-triage@grainmart
```

Needs `kubectl`, `helm` and Python 3.10+ on your machine. The first start creates a private virtualenv
in the plugin's data directory. Defaults: namespace `gm-sandbox`, context `kind-gulimall`; override
with `PLATFORM_NAMESPACE` and `PLATFORM_KUBE_CONTEXT`.

Then in Claude Code: `/grainmart-triage:triage gulimall-review is crash-looping`.

## How it's checked

| Check | Command |
|---|---|
| Manifest, skill and MCP config | `claude plugin validate platform/claude-plugin/grainmart-triage --strict` |
| Server: read-only enforcement, pinning, masking, every tool | `cd server && pytest -q` (26 tests) |
| Does the plugin make Claude better at triage? | `claude plugin eval` against a real fault in the kind sandbox ([results](evals/RESULTS.md)) |
