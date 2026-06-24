# Kind local validation (full Java stack, no ECR)

## Prerequisites

- Docker Desktop running
- `brew install kind kubectl helm`
- Root `.env` with `RDS_*` (Oregon RDS reachable from your Mac)

## One-shot

```bash
cd /path/to/gulimall
chmod +x k8s/scripts/*.sh
./k8s/scripts/kind-up.sh
./k8s/scripts/k8s-create-secrets.sh
./k8s/scripts/kind-deploy.sh
```

## Verify

```bash
kubectl get pods -n gulimall
curl -s http://localhost:3088/actuator/health
```

Gateway: http://localhost:3088 (not :88 — avoids conflict with Compose gateway).

**Memory:** Full stack on one kind node needs **~12GB** Docker RAM. If pods stay `Pending`/`OOMKilled`, raise Docker Desktop memory or stop other stacks.

Mall UI: `cd gulimall-mall && NEXT_PUBLIC_API_BASE=http://localhost:3088 npm run dev`

## Tear down

```bash
kind delete cluster --name gulimall
```

See [docs/EKS.md](../docs/EKS.md) for **full-stack EKS** deployment:

```bash
./k8s/scripts/eks-up.sh
```

Load testing and HPA: [docs/load-test.md](../docs/load-test.md) — `brew install k6` then `./k8s/scripts/run-load-test.sh`.

CI/CD: [docs/CD.md](../docs/CD.md) — GitHub Actions → ECR → Helm on `main`.
