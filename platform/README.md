# GrainMart platform kit

Tools that make the 12-service GrainMart stack easier and safer to change: a golden path for new
services, guardrails every workload must pass, and a read-only way for an AI agent to triage incidents.

```
gm new service ──► module + chart entry + build lists ──► gm policy (OPA guardrails) ──► gm sandbox deploy (kind)
                                                                                              │
                     Claude Code ◄── platform MCP server (read-only kubectl/helm, audited) ◄──┘
```

## 1. Golden path: `gm new service` (Go CLI, `platform/gm`)

Adding a service used to mean hand edits in six places: a new Maven module, the root `pom.xml`, the
chart's `images:` and `apps:` blocks, and both lists in `k8s/scripts/common.sh`. Forget one and the
service builds but never deploys.

```bash
cd platform/gm && go build -o gm . && cd ../..
platform/gm/gm new service review --port 15000 --description "Product review service"   # 6 files, 3 registrations
platform/gm/gm policy                 # guardrails on the kind and EKS renders
platform/gm/gm sandbox deploy review  # build, load into kind, deploy alone into gm-sandbox
platform/gm/gm doctor                 # 12 preflight checks, each with a fix
```

- **What it generates:** a Spring Boot module (health probes, Consul registration, an endpoint and a
  WebMvc test), a Dockerfile that runs as UID 1000, and the chart entry.
- **Validation before writing:** names and ports are checked against the chart (`port 12000 is already
  used by coupon`), and every edit is computed before any file is written, so a refusal changes nothing.
- **Sandbox deploys** use their own release and namespace and only Consul as a dependency, so the main
  `gulimall` release is never touched. Each build is tagged by its image ID; reusing `:local` would leave
  the old pod running after a rebuild (this happened, and is why it changed).
- **`gm doctor`** also catches manual drift: an app in the chart without a Dockerfile, pom entry or
  build-list entry.

Measured on kind (Apple M4, Docker Desktop): `gm new service` to a healthy pod in the sandbox in about
80 s, almost all of it waiting for Spring Boot and the readiness probe.

**A bug the golden path found.** The first generated service crash-looped with
`NoClassDefFoundError: StringRedisTemplate`. `gulimall-common` registers its Redis cache as
auto-configuration for every service, but Redis is an optional dependency, so any service without Redis
on its classpath fails at startup. Coupon, ware and third-party also lack Redis on their classpath; I
haven't confirmed whether their current images are affected.
`ProtectedCacheConfig` now has `@ConditionalOnClass(name = "...StringRedisTemplate")`, with a test that
fails without it.

## 2. Guardrails: policy as code (`platform/policy`, OPA/Conftest)

Every Deployment, StatefulSet, DaemonSet and Job rendered from the chart must have CPU and memory
requests, a memory limit, readiness and liveness probes (Jobs excepted), a pinned image tag (no
`:latest`), a non-root user unless annotated `policy.grainmart/allow-root` with a reason, and no
secret-looking env var set as a literal. Rules have their own tests (`conftest verify`, 10 tests).

| Chart | Before | After |
|---|---|---|
| kind render | 68 violations in 16 workloads | 0 |
| EKS render (CD's pinned image tag) | 78 | 0 |

Fixes: default requests and limits per environment sized to the JVM heap; apps run as UID 1000
(spot-checked first by starting the coupon, gateway and renren-fast images as UID 1000: no permission
errors); liveness probes for Consul, Elasticsearch,
RabbitMQ, Redis and Jaeger; Elasticsearch, Jaeger and the config-seed Job run as their images'
non-root users; Consul, Redis and RabbitMQ are annotated because their official images start as root
and drop privileges themselves.

## 3. Agent triage: read-only MCP server (`platform/mcp`)

`platform_mcp.py` gives an agent nine tools over one namespace: pods with waiting reasons, events,
describe, previous-run logs, workload config, Services, Helm history, **release drift** (live vs what
Helm deployed) and runbook search.

- **Read-only:** only `get`, `describe` and `logs`, plus helm `history`, `status` and `get manifest`.
  Secrets can't be read. Namespace and context are fixed by the server.
- **Audited and masked:** every call is appended to an audit log, and credential-looking values are
  masked, inside parsed JSON so the JSON stays valid.
- **Tests:** 23 tests, using a real MCP client in-process.

**Evaluation:** three realistic faults injected into the sandboxed service, each diagnosed by Claude Code
using only these tools ([sessions](agent-sessions/README.md), $0.87 in total):

| Fault | Root cause found | Notes |
|---|---|---|
| Image tag that was never built | ✅ | Named the Helm revision and suggested rollback |
| Secret referenced but missing | ✅ | The first run exposed a server bug (masking broke JSON); fixed and re-run |
| Consul host typo set with `kubectl set env` | ✅ | The first run blamed the install; adding `release_drift` and `list_services` let the re-run attribute it correctly |

Across all sessions the audit log has 44 calls and no writes.

Try it: `platform/faults/inject.sh wrong-config`, then run `claude` in the repo root with
`--mcp-config platform/mcp/mcp.json`, or use `platform/agent-sessions/run.sh`.

## CI

`.github/workflows/platform.yml`: Rego unit tests, `go vet` and `go test` (including a generated service
passing the guardrails), `gm policy` on the real chart, and the MCP server tests.

## Not done

- **The main kind and EKS releases** haven't been redeployed with the new requests, limits and non-root
  settings. The non-root change was spot-checked on three images and on the generated service; a full
  redeploy is the next step.
- **The sandbox fault tests use one service.** Faults in shared infrastructure (Consul down, node
  pressure) are not covered.
