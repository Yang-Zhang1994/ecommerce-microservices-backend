# Runbook: CrashLoopBackOff

The container starts and exits. The answer is almost always in the logs of the *previous* run
(`pod_logs` with `previous=true`): look for the first `Caused by:` or `APPLICATION FAILED TO START`.

| Log shows | Usual cause | Fix |
|---|---|---|
| `UnknownHostException` / `Connection refused` to consul, redis, rabbitmq | A dependency host or port in the workload's env is wrong, or the dependency is not running in this namespace | Compare the env in `workload_config` with the Service names in the namespace; correct the value in the chart |
| `NoClassDefFoundError` | A dependency missing from the image | Rebuild; check the module's pom |
| `OOMKilled` as last exit reason | Memory limit below JVM heap plus overhead | Raise `resources.limits.memory` or lower `-Xmx` |
| Exits only after ~60-90 s | Liveness probe kills it before it is ready | Raise `probeDelay` for the app |
