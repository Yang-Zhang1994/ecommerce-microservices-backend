# Agent triage sessions

Each session: one fault injected into the sandboxed review service (`platform/faults/inject.sh`), then
`platform/agent-sessions/run.sh` runs Claude Code (claude-opus-5-5, headless) with only the read-only
platform MCP tools and this prompt:

> The review service in the gm-sandbox namespace is down. Using only the platform tools, find the root cause and give the exact fix. Don't guess: say what you saw that shows it. You can't change anything, so describe the fix for a human to apply.

| Session | Fault | How it was injected | Result | Turns | Cost |
|---|---|---|---|---|---|
| [bad-image](#bad-image) | Release points at an image tag that was never built | `helm upgrade --set images.review.tag=sbx-4e1f00c0ffee` | Correct: named Helm revision 2, the tag, ErrImageNeverPull; proposed rollback | 13 | $0.24 |
| [missing-secret-before-fix](#missing-secret-before-fix) | Deployment reads a password from a Secret that doesn't exist (first run) | `kubectl patch: env from secretKeyRef review-db/password` | Correct cause, but list_pods and workload_config errored: masking credentials in raw JSON text broke the JSON. Fixed, regression test added | 15 | $0.13 |
| [missing-secret](#missing-secret) | Same fault, after the fix | `same` | Correct: CreateContainerConfigError, secret "review-db" not found; create the Secret or drop the reference | 9 | $0.09 |
| [wrong-config-before-drift-tool](#wrong-config-before-drift-tool) | Consul host typo, set by hand outside Helm (first run) | `kubectl set env SPRING_CLOUD_CONSUL_HOST=consul-server` | Correct cause (UnknownHostException), but wrongly said the value came with the install, and couldn't list Services. Added release_drift and list_services | 10 | $0.18 |
| [wrong-config](#wrong-config) | Same fault, with the new tools | `same` | Correct: release_drift showed consul (Helm) vs consul-server (live); re-sync from Helm | 9 | $0.23 |

Total cost of all five sessions: $0.87. The audit log (`platform/mcp/audit.log`, not committed) recorded every kubectl and helm call; none was a write.

## bad-image

Tool calls: `list_pods()` → `recent_events()` → `pod_logs(pod=gulimall-review-5f744f6587-lxnjd, previous=True)` → `describe_pod(pod=gulimall-review-6c86b4678b-8zjl4)` → `release_history()` → `workload_config(name=gulimall-review)` → `describe_pod(pod=gulimall-review-5f744f6587-lxnjd)` → `pod_logs(pod=gulimall-review-5f744f6587-lxnjd)` → `search_runbooks(query=ErrImageNeverPull)` → `list_pods()` → `recent_events(only_warnings=False)`

Answer:

I found the root cause: the latest deploy points the review service at an image that isn't on the node.

**Root cause:** Helm revision 2 (deployed 13:41:10) changed the `gulimall-review` image to `gulimall-review:sbx-4e1f00c0ffee`. That image isn't on the kind node, and the pull policy is `Never`, so Kubernetes can't start the container.

**What I saw:**
- **Release history:** revision 1 (13:38:18) is `superseded` and revision 2 (13:41:10) is `deployed` and current.
- **Workload config** for `gulimall-review`: `"image":"gulimall-review:sbx-4e1f00c0ffee"`, `"imagePullPolicy":"Never"`, `"available":0`.
- **The new pod `gulimall-review-6c86b4678b-8zjl4`** is stuck in `Waiting`, reason `ErrImageNeverPull`. The kubelet event says: `Container image "gulimall-review:sbx-4e1f00c0ffee" is not present with pull policy of Never`. Its restart count is 0, so the container never started and the app itself isn't failing.
- **The previous pod `gulimall-review-5f744f6587-lxnjd`** (revision 1) ran image `gulimall-review:sbx-312534f79b46`. The event said that image was "already present on machine", and the container was created and started. At the rollout it was stopped and deleted ("Scaled down replica set gulimall-review-5f744f6587 from 1 to 0"), so nothing is serving now.
- **The runbook** `pod-not-starting.md` covers this: `ErrImageNeverPull` on kind means the release's image tag isn't on the node, "often a typo or a build that never ran".

**Fix (for a human to apply), either one:**
1. **Restore service now:** roll back to revision 1, which uses an image that is on the node:
   ```
   helm rollback gm-sandbox 1 -n gm-sandbox
   ```
2. **Ship the new version:** build the image and load it onto the node, then redeploy:
   ```
   gm sandbox deploy review
   ```
   Before doing this, check that `4e1f00c0ffee` is a real commit. The "c0ffee" ending looks like a placeholder or typo. If it is one, set the review image tag in the values to the correct tag and upgrade again.

Don't switch `imagePullPolicy` away from `Never` to fix this. On kind, the image has to be loaded locally either way.

**What I couldn't confirm:**
- **Revision 1's stability:** the old pod had one `BackOff` (restart) event about 20 seconds after it was scheduled, before it started again. It came up at the same time as Consul, which may explain it, but I can't confirm that. The pod is deleted, so its logs are gone (`pod_logs` returned nothing and `describe_pod` was empty). After a rollback, check that the pod becomes Ready and stays up.
- **Pod list:** `list_pods` failed every time I called it, so I worked from events, `describe_pod` and the workload config instead.

## missing-secret-before-fix

Tool calls: `list_pods()` → `recent_events()` → `describe_pod(pod=gulimall-review-6fcccb74c-bfc76)` → `pod_logs(pod=gulimall-review-76989ddc49-wdm7k, previous=True)` → `workload_config(name=gulimall-review)` → `release_history()` → `describe_pod(pod=gulimall-review-76989ddc49-wdm7k)` → `pod_logs(pod=gulimall-review-76989ddc49-wdm7k)` → `workload_config(name=gulimall-review)` → `list_pods()` → `recent_events(only_warnings=False)` → `search_runbooks(query=secret "review-db" not found)` → `workload_config(name=review)`

Answer:

**Root cause:** the review pod can't start because its Deployment needs a Kubernetes Secret called `review-db`, and that Secret doesn't exist in `gm-sandbox`. The container is never created, so the service has no running pod.

**What I saw:**

- **`describe_pod gulimall-review-6fcccb74c-bfc76`:**
  - `State: Waiting, Reason: CreateContainerConfigError`, `Restart Count: 0`.
  - The environment includes `REVIEW_DB_PASSWORD: ... key 'password' in secret 'review-db' ... Optional: false`.
  - Events show `Warning Failed (x3) kubelet Error: secret "review-db" not found`.
- **`recent_events`:** the same `secret "review-db" not found` warning keeps repeating for this pod.
- **The team runbook (`pod-not-starting.md`)** matches this: `CreateContainerConfigError` means "the pod references a Secret ... that does not exist in the namespace. Create the missing Secret/key, or remove the reference."

**The exact fix (pick one):**

1. **If the service really needs the DB password:** create the Secret with a `password` key in the right namespace:
   ```
   kubectl -n gm-sandbox create secret generic review-db --from-literal=password='<review DB password>'
   ```
   The kubelet keeps retrying, so the pod should start on its own. To force it: `kubectl -n gm-sandbox rollout restart deploy/gulimall-review`.
2. **If the reference was added by mistake:** remove the `REVIEW_DB_PASSWORD` `secretKeyRef` from the review Deployment's template (`k8s/helm/gulimall/templates/app-microservices.yaml`) and redeploy. If you keep it, also add the Secret to the chart or the deploy steps so the next sandbox install doesn't hit this again.

**What I couldn't confirm:**

- **Where the change came from.** The Helm history shows only revision 1 (installed 13:43:11). But the events show a second ReplicaSet, `6fcccb74c`, created about 1.5 minutes later, around 13:44:53, replacing `76989ddc49`. So the Deployment was most likely edited outside Helm. That's an inference, not something I saw directly. I couldn't check it because `workload_config` and `list_pods` both returned errors every time I tried.
- **Why the earlier pod crashed.** The first pod, `gulimall-review-76989ddc49-wdm7k`, was in CrashLoopBackOff, so the service was already failing before the Secret reference appeared. That pod was deleted before I could read its logs: `describe_pod` came back empty and the previous-run logs said "(no logs)". If the rollout after the fix goes back to that crash, check the new pod's previous-run logs (`pod_logs` with `previous=true`) for the first `Caused by:` line.

## missing-secret

Tool calls: `list_pods()` → `recent_events()` → `workload_config(name=gulimall-review)` → `release_history()` → `describe_pod(pod=gulimall-review-6fcccb74c-bfc76)` → `search_runbooks(query=secret review-db not found)` → `pod_logs(pod=gulimall-review-76989ddc49-wdm7k, previous=True)`

Answer:

I found the cause: the review pod can't start because it needs a Kubernetes Secret called `review-db`, and that Secret doesn't exist in `gm-sandbox`.

**What I saw**
- **`list_pods`:** `gulimall-review-6fcccb74c-bfc76` is `Pending` and has never started (0 restarts). Its waiting reason is `CreateContainerConfigError` with the message `secret "review-db" not found`. The only other pod, `consul`, is running and ready.
- **`describe_pod`:** the image is already on the node (`Pulled ... already present`), so it isn't an image problem. The container fails before it starts, with `Error: secret "review-db" not found`, 8 times in 90s. The env shows `REVIEW_DB_PASSWORD` comes from key `password` in secret `review-db`, with `Optional: false`.
- **`workload_config`:** the Deployment has the same reference, `secretKeyRef: {name: review-db, key: password}`. All the other env values are plain literals.
- **`release_history`:** there is one Helm revision (rev 1, "Install complete", 13:43). There's no earlier version to roll back to, and the chart is pulling in a Secret it doesn't create.
- **Runbook `pod-not-starting.md`:** for `CreateContainerConfigError`, the pod refers to a Secret or key that doesn't exist. The fix is to create it or remove the reference.

**Fix for a human to apply**

Create the Secret with a `password` key in the namespace:

```bash
kubectl -n gm-sandbox create secret generic review-db \
  --from-literal=password='<review DB password>'
```

Kubernetes retries this pod about every 10–20s, so it should start within a minute; you don't need to restart anything. If it doesn't come up, run `kubectl -n gm-sandbox rollout restart deploy/gulimall-review`.

If the review service shouldn't need a database password in the sandbox, the other fix is to remove the `REVIEW_DB_PASSWORD` `secretKeyRef` from the chart's values for this environment and run `helm upgrade`. Either way, the chart or the sandbox setup should be changed to create `review-db`, or this will happen again on the next fresh install.

**Something else to check:** the events also show an older pod, `gulimall-review-76989ddc49-wdm7k`, that was in `BackOff` ("restarting failed container") about 3 minutes earlier. That pod no longer exists and its previous logs came back empty, so I can't tell why it crashed. The new pod has never run, so once the Secret is in place, watch its first start. If it goes into CrashLoopBackOff, read `pod_logs` with `previous=true` to find the first `Caused by:`, since this may be a second problem.

## wrong-config-before-drift-tool

Tool calls: `list_pods()` → `recent_events()` → `pod_logs(pod=gulimall-review-77d757dc4f-5hbvl, previous=True)` → `workload_config(name=gulimall-review)` → `release_history()` → `search_runbooks(query=UnknownHostException consul-server)` → `describe_pod(pod=consul-6685bdcd94-fxd2l)` → `describe_pod(pod=gulimall-review-77d757dc4f-5hbvl)`

Answer:

I found the root cause. The review app can't resolve the Consul hostname it was given, so it crashes on startup.

**What I saw**

1. **`list_pods`:** `gulimall-review-77d757dc4f-5hbvl` is in `CrashLoopBackOff` (3 restarts, exit code 1). The only other pod in the namespace is `consul-6685bdcd94-fxd2l`, which is Running and Ready.
2. **`pod_logs` (previous run):** startup fails while registering with Consul:
   ```
   ConsulServiceRegistry.register → AgentConsulClient.agentServiceRegister
   Caused by: com.ecwid.consul.transport.TransportException: java.net.UnknownHostException: consul-server
   ```
   Each run dies about 4 seconds after starting (13:49:26 to 13:49:30). That rules out the liveness probe (75s delay) and memory (the exit is `Error`, not `OOMKilled`).
3. **`workload_config` / `describe_pod`:** the Deployment sets `SPRING_CLOUD_CONSUL_HOST=consul-server` and `SPRING_CLOUD_CONSUL_PORT=8500`.
4. **Consul itself is healthy:** its pod is labelled `app=consul`, its ReplicaSet is `consul-…`, it listens on 8500/TCP, its readiness check (`consul members`) passes, and it has 0 restarts. Consul isn't down. The review app is pointed at a name that doesn't resolve in `gm-sandbox`.
5. **The runbook (`crashloop.md`)** covers this case: "`UnknownHostException` to consul… A dependency host… in the workload's env is wrong… correct the value in the chart."
6. **`release_history`:** there's only one revision, the initial install at 13:47. So the bad value came with the install, not a later upgrade.

**Root cause:** the chart gives gulimall-review `SPRING_CLOUD_CONSUL_HOST=consul-server`, and that name doesn't resolve in `gm-sandbox`. The Consul instance here is the `consul` workload.

**Fix (for a human to apply)**

1. Confirm the actual Consul Service name. The platform tools don't list Services, so I couldn't see it. Everything points to `consul`, but check first:
   ```
   kubectl -n gm-sandbox get svc -l app=consul
   ```
2. In the gulimall Helm chart, change the review service's Consul host from `consul-server` to that Service name, probably `consul`. Keep port `8500`. The result should be:
   ```
   SPRING_CLOUD_CONSUL_HOST=consul
   ```
   Make the change wherever the chart sets it for gulimall-review: the values file used for this sandbox, or the env block in the microservices template.
3. Roll out with `helm upgrade gm-sandbox …` using the same values file plus the fix. Don't hand-edit the Deployment with `kubectl set env`, because the next Helm upgrade would overwrite it.
4. Check that the new pod reaches Ready, that the logs show it registered with Consul, and that there are no more `BackOff` events.

**Two other things I noticed (not the cause):**
- The pod's image tag is reported inconsistently. `list_pods` shows `gulimall-review:sbx-312534f79b46`, while the Deployment and `describe_pod` show `sbx-3372253ac98d`.
- Events also show BackOff from a second ReplicaSet, `gulimall-review-786c9b7c85`.

Both ReplicaSets would fail the same way while the host value is wrong. After the fix, check that only one ReplicaSet remains and it's running the image you expect.

## wrong-config

Tool calls: `list_pods()` → `recent_events()` → `pod_logs(pod=gulimall-review-77d757dc4f-5hbvl, previous=True)` → `workload_config(name=gulimall-review)` → `release_drift()` → `list_services()` → `release_history()`

Answer:

**Root cause:** someone ran a `kubectl` edit outside Helm and set the review Deployment's `SPRING_CLOUD_CONSUL_HOST` to `consul-server`. No Service by that name exists in `gm-sandbox`. The Consul Service is called `consul`. Review can't resolve the hostname, so it fails at startup and keeps restarting.

**What I saw:**

1. **`list_pods`:** `gulimall-review-77d757dc4f-5hbvl` is in `CrashLoopBackOff`, with 4 restarts and a last exit code of 1. The `consul` pod is Running and Ready, so Consul itself is fine.
2. **`pod_logs` (previous run):** the app dies while registering with Consul:
   ```
   Caused by: com.ecwid.consul.transport.TransportException: java.net.UnknownHostException: consul-server
     at ...AgentConsulClient.agentServiceRegister(...)
     at ...ConsulServiceRegistry.register(...)
   ...
   Application run failed
   ```
3. **`workload_config gulimall-review`:** the live env has `SPRING_CLOUD_CONSUL_HOST=consul-server` and `SPRING_CLOUD_CONSUL_PORT=8500`.
4. **`list_services`:** the only Consul Service is `consul`, ClusterIP, port 8500. There is no `consul-server`.
5. **`release_drift`:** this is the only drift in the release. On `gulimall-review`, `env SPRING_CLOUD_CONSUL_HOST` is `consul` in the Helm release and `consul-server` live. Helm installed the correct value and the cluster was changed by hand afterwards.
6. **`release_history`:** there is only revision 1 (install, status `deployed`), so no Helm upgrade caused this.

**Fix (for a human to apply):** put the Helm value back. Either of these works:

- Re-sync from Helm, which overwrites the hand edit:
  ```
  helm upgrade gm-sandbox <chart> -n gm-sandbox --reuse-values
  ```
  (or whatever upgrade command you normally run for this release).
- Or patch it directly:
  ```
  kubectl -n gm-sandbox set env deployment/gulimall-review SPRING_CLOUD_CONSUL_HOST=consul
  ```

The Helm route is better because it leaves no drift behind. Either way, the env change starts a new rollout. To confirm the fix, watch for the new pod to become Ready: the readiness probe is `/actuator/health` with a 45s initial delay. Then check that `release_drift` comes back empty.

**Something else I noticed (not the cause):** the crashing pod runs image `gulimall-review:sbx-312534f79b46`, but the Deployment spec says `sbx-3372253ac98d`. The events also show an earlier review pod (`786c9b7c85-b9lf8`) in BackOff. That means more than one ReplicaSet has been rolled out, and the pod I examined may not be from the newest spec. Check `kubectl rollout status deployment/gulimall-review -n gm-sandbox` after the fix. Whichever image runs, the logs show the same Consul hostname failure. The drift check didn't flag the image, so this mismatch doesn't change the fix above.
