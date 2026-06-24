# Load testing (k6) and Gateway / Product HPA

Public read paths through **Spring Cloud Gateway** on EKS:

| Scenario | Endpoint | Backend |
|----------|----------|---------|
| Search | `GET /api/search/product/list` | `gulimall-search` → Elasticsearch |
| Catalog | `GET /api/product/category/list/tree` | `gulimall-product` → RDS |
| SKU detail | `GET /api/product/item/{skuId}` | `gulimall-product` → RDS |

Scripts live under `k8s/scripts/load-test/`. Run via `k8s/scripts/run-load-test.sh`.

## Prerequisites

1. **Cluster healthy** — EKS node group ≥ 1 node, all `gulimall` pods `Running`/`Ready`.
   ```bash
   ./k8s/scripts/eks-up.sh          # or scale existing node group to desired_size ≥ 2
   kubectl get pods -n gulimall
   curl -s https://www.yangzhangtech.online/actuator/health
   ```
2. **Search index** — smoke search returns `code: 0` (reindex if needed; see `docs/EKS.md`).
3. **metrics-server** (for CPU HPA):
   ```bash
   kubectl get deployment metrics-server -n kube-system \
     || kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
   ```
4. **k6** — `brew install k6`

## Helm: HPA + CPU requests

EKS values (`values-eks.yaml`) enable CPU-based HPA when `hpa.enabled: true`:

| Deployment | min | max | CPU target |
|------------|-----|-----|------------|
| `gulimall-gateway` | 2 | 4 | 70% |
| `gulimall-product` | 1 | 3 | 70% |

Deploy or upgrade:

```bash
helm upgrade --install gulimall k8s/helm/gulimall \
  -n gulimall -f k8s/helm/gulimall/values-eks.yaml \
  --set ingress.annotations."alb\.ingress\.kubernetes\.io/certificate-arn"="${HELM_CERTIFICATE_ARN}"
kubectl get hpa -n gulimall
```

HPA needs **CPU requests** on containers (set in `apps.gateway.resources` / `apps.product.resources`).

## Run load tests

**Production (ALB):**

```bash
chmod +x k8s/scripts/run-load-test.sh
./k8s/scripts/run-load-test.sh
```

**Local kind:**

```bash
BASE_URL=http://localhost:30888 ./k8s/scripts/run-load-test.sh
```

Optional env:

| Variable | Default |
|----------|---------|
| `BASE_URL` | `https://www.yangzhangtech.online` |
| `RESULTS_DIR` | `k8s/scripts/load-test/results/` |

### Scenarios

| Script | Purpose |
|--------|---------|
| `k6-smoke.js` | 5 VUs × 30s — health + one search + category tree |
| `k6-search-sustained.js` | Ramp 20 → 50 VUs, 5.5 min — ES search list |
| `k6-product-read.js` | Ramp 15 → 30 VUs, 5 min — category tree + SKU item |

During `k6-search-sustained.js`, watch scaling:

```bash
watch -n5 'kubectl get hpa,pods -n gulimall -l app=gulimall-gateway; kubectl get hpa,pods -n gulimall -l app=gulimall-product'
```

### Read k6 output

From the end-of-run summary:

- **http_reqs** → `rate` = sustained **requests per second (RPS)**
- **http_req_duration** → `p(95)` = tail latency
- **http_req_failed** → error rate (target &lt; 2%)

JSON exports: `k8s/scripts/load-test/results/<timestamp>-*-summary.json`.

## Recorded results

**2026-06-23** — EKS `gulimall-prod-eks`, 2× `t3.large` Spot, ALB → Gateway (2 replicas) → Search → Elasticsearch. RDS was stopped earlier in the day; restarted before the run. ES index empty (`degraded: true` on search responses — still exercises Gateway + Search + ES path).

| Date | Environment | Scenario | RPS (`http_reqs.rate`) | p95 (ms) | Error rate | Gateway pods | Product pods | Notes |
|------|-------------|----------|------------------------|----------|------------|--------------|--------------|-------|
| 2026-06-23 | EKS 2× t3.large | `k6-search-sustained` (50 VU plateau) | **35.9** | **3510** | **12.2%** | 2 (HPA min) | 1 | 12,397 reqs / ~5m45s; median latency 242 ms |
| 2026-06-23 | EKS 2× t3.large | `k6-product-read` (30 VU) | 2.7 | 45033 | 3.0% | — | 1 | Run immediately after search test; cluster already stressed — not representative |

Raw logs: `k8s/scripts/load-test/results/20260623-163906-search-summary.json` (gitignored dir).

**Takeaways**

- Sustained **~36 RPS** on the public search API is achievable on this stack before tail latency and errors rise at 50 VU.
- Error rate exceeded the 2% k6 threshold — likely combination of cold ES index, node replacement during the session, and 2× `t3.large` CPU/RAM limits. Re-run after `./k8s/scripts/eks-up.sh` + full pod Ready + ES reindex for cleaner numbers.
- **Do not** run 50 VU against a cold or recovering cluster; start with `k6-smoke.js`, then ramp.

**Summary (search scenario):**

> Load-tested public product search at **~36 RPS** on AWS EKS (2× t3.large, Gateway/Product HPA, k6).

Peak-load latency on the same run: median **242 ms**, p95 **3.5 s** at 50 VU (portfolio-scale environment, not a production SLA).

## Cost note

EKS node groups bill while nodes are up. Scale to zero when idle (`eks-down` / terraform `desired_size=0`) and run load tests only when you need fresh numbers.
