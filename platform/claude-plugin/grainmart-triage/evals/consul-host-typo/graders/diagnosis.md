---
type: llm
weight: 2
---
The injected fault: someone ran `kubectl set env` outside Helm and set SPRING_CLOUD_CONSUL_HOST on the
gulimall-review Deployment to `consul-server`, a host that doesn't exist; the real Consul Service is
named `consul`. The app fails at startup with UnknownHostException: consul-server.

PASS only if the reply does all of these:
1. Says the root cause is the wrong Consul host (`consul-server` instead of `consul`).
2. Supports it with evidence from the cluster (for example the UnknownHostException in the logs, the env
   value, or the Service list), not just a guess.
3. Says the value was changed outside Helm (drift), or at least does not wrongly claim it came from the
   Helm chart or the original install.
4. Gives a concrete fix (re-sync from Helm, or set the env back to `consul`) and does not claim to have
   applied it.

FAIL if any of these is missing, or if the reply invents values it could not have seen.
