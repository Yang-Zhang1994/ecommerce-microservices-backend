# Distributed configuration with Consul KV

Product and order services load configuration from Consul KV on top of their
`application.yml` defaults (`spring.config.import: optional:consul:`).

| Consul key | Used by | Contents | When changes apply |
|---|---|---|---|
| `config/application/data` | every service that imports `consul:` | `gulimall.cache.protected.*` (cache TTL, jitter, null TTL, lock timings) | **Live**: the Consul watch long-polls, and `@ConfigurationProperties` beans are rebound. `ProtectedCache` reads them on every call. |
| `config/gulimall-order/data` | order service | payment timeouts, outbox batch size | On restart (`@Value` fields) |

Precedence (highest first): environment variables → `config/<service>/data` →
`config/application/data` → `application.yml`. Secrets stay in env vars or AWS
Secrets Manager, never in Consul KV.

## Source of truth

The YAML files in `k8s/helm/gulimall/consul-config/` are the only place to edit config.

- **Kubernetes:** a Helm `post-install,post-upgrade` hook Job (`consul-config-seed`)
  writes each file to `config/<file name>/data`, so a `helm upgrade` rolls out config
  the same way it rolls out code.
- **Local:** `docker compose up -d consul && k8s/scripts/seed-consul-config.sh`

## Change a cache TTL without a restart

```bash
kubectl port-forward svc/consul 8500 &
# edit k8s/helm/gulimall/consul-config/application.yml, then:
k8s/scripts/seed-consul-config.sh
```

Product pods pick the new value up within about a second.

## Test

`ConsulConfigLiveReloadTest` (gulimall-product) starts a Spring context against a real
Consul agent, checks the value loaded at startup, changes the key, and asserts the running
context sees the new value. It uses a random KV prefix and is skipped when Consul is not
reachable.

```bash
docker run -d --rm -p 8501:8500 hashicorp/consul:1.17 agent -dev -client=0.0.0.0
mvn -pl gulimall-product -am test -Dproduct.skipTests=false \
  -Dtest=ConsulConfigLiveReloadTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dconsul.url=http://localhost:8501
```
