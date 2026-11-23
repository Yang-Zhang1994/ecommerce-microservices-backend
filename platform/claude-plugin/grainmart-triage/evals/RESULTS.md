# Eval results: grainmart-triage

Run with `claude plugin eval . --mocks off --allow-tools "mcp__plugin_grainmart-triage_platform__*"`
against a real fault in the kind sandbox (`platform/faults/inject.sh wrong-config`: a `kubectl set env`
outside Helm points gulimall-review at Consul host `consul-server`; the Service is `consul`). Model
claude-opus-5-5, 3 runs per arm. The baseline arm runs the same prompt without the plugin.

Graders: the answer names `consul-server`; `release_drift` was called; an LLM judge (2 of 3 votes)
checks root cause, cluster evidence, drift attribution, and a fix not claimed as applied (weight 2).

| Run | With plugin | Without plugin | Cost |
|---|---|---|---|
| 2026-11-23, first | 0.25 (0/3 pass) | 0 (0/3) | $0.82 |
| 2026-11-23, after fixes | **1.0 (3/3 pass)** | 0 (0/3) | $1.08 |

## What the first run found

All three plugin runs reported "the namespace looks completely empty". The eval runs MCP servers with
a different HOME, so kubectl could not find its kubeconfig. The server dropped kubectl's error and
returned empty output, which the agent reasonably read as "no pods". Two fixes followed:

- **Failures are errors now.** A non-zero kubectl or helm exit becomes a tool error carrying the
  message. Refusals also had their reasons hidden by the MCP SDK until both became `ToolError`s.
  Regression tests cover both.
- **`bin/serve.sh` finds the real kubeconfig** when HOME has been changed.

## A passing answer (run 1, after fixes)

**gulimall-review is crash-looping because someone changed its Consul host outside Helm to `consul-server`, a name that doesn't exist in gm-sandbox.** The Consul Service is called `consul`, and that's the value Helm deployed. At startup the app tries to register with Consul, can't resolve the name, and exits with code 1.

**Evidence**
- `list_pods`: `gulimall-review-84dc895555-79zzl` is in `CrashLoopBackOff` with 5 restarts, last exit `Error`, exit code `1`. Consul's own pod is running and ready.
- `pod_logs` from the run before the last restart: `Caused by: java.net.UnknownHostException: consul-server`. It fails in `ConsulServiceRegistry.register`, so startup aborts with `Failed to start bean 'webServerStartStop'`.
- `workload_config`: `SPRING_CLOUD_CONSUL_HOST=consul-server`, `SPRING_CLOUD_CONSUL_PORT=8500`.
- `list_services`: the only Consul Service is `consul` (ClusterIP, port 8500). There is no `consul-server`.
- `release_drift`: `env SPRING_CLOUD_CONSUL_HOST`, Helm release value `consul`, live value `consul-server`. Someone ran something like `kubectl edit` or `kubectl set env` on the Deployment.
- `release_history`: there's only revision 1 ("Install complete"), so no Helm change caused this.
- The runbook `crashloop.md` matches: an `UnknownHostException` to Consul means the dependency host in the env is wrong.

**Fix for a human**
The chart is already correct, so no chart change is needed. Put the live Deployment back to the Helm value:

```sh
kubectl -n gm-sandbox set env deployment/gulimall-review SPRING_CLOUD_CONSUL_HOST=consul
```

Or re-apply the release with `helm rollback gm-sandbox 1 -n gm-sandbox`. Either way, find out who made the manual edit and why, so it doesn't come back.

To check it worked:
```sh
kubectl -n gm-sandbox rollout status deployment/gulimall-review
kubectl -n gm-sandbox get pods -l app=gulimall-review   # want 1/1 Ready, restarts not climbing
kubectl -n gm-sandbox logs deploy/gulimall-review | grep -i consul   # no UnknownHostException
```
Running `release_drift` again should come back empty.

**Not confirmed**
- I don't know who made the edit or when. The tools don't show that.
- The images don't match. The crashing pod runs `gulimall-review:sbx-312534f79b46`, but the Deployment spec now says `gulimall-review:sbx-7c0f838af1bc`. `release_drift` didn't flag the image, so this may be a rollout still in progress rather than a second manual edit. I couldn't confirm which. Events also show an earlier ReplicaSet pod (`gulimall-review-64cc445b5f-7nkp4`) backing off. After the fix, check that the new pod runs the image you expect.
- The pod uses `imagePullPolicy: Never`, so the image has to already exist on the node. If the new pod shows `ErrImageNeverPull`, that's a separate problem.

I haven't run any of these commands. The platform tools are read-only.
