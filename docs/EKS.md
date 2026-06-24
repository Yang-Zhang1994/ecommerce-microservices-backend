# Gulimall on Kubernetes (kind + EKS)

| Environment | Platform | Cost | Use case |
|-------------|----------|------|----------|
| Local full stack | Docker Compose | $0 | 12 services |
| Local K8s | **kind** + Helm | $0 | Full stack dev (`values-kind.yaml`) |
| Cloud | **EKS** (us-west-2) | ~$73 control plane + Spot nodes | **Full mall** via Helm + ALB + Oregon RDS |

Region: **us-west-2** (same as RDS and Terraform).

## Prerequisites

- Docker, `kubectl`, `helm`, `kind` (local only)
- Java 17, Maven
- AWS CLI + Terraform (EKS)
- Root `.env` with `RDS_*`, `ORDER_STRIPE_*`, optional `AWS_ACCESS_KEY_ID` for third-party (see `.env.example`)

---

## Quick start — kind (local)

```bash
chmod +x k8s/scripts/*.sh
./k8s/scripts/kind-up.sh
./k8s/scripts/k8s-create-secrets.sh
./k8s/scripts/kind-deploy.sh
```

Gateway: http://localhost:3088

---

## Quick start — EKS full stack

One command (Terraform + ECR push + ALB controller + Helm):

```bash
chmod +x k8s/scripts/*.sh
./k8s/scripts/eks-up.sh
```

Or step by step:

### 1. Terraform (`enable_eks=true`)

Creates:

- EKS cluster `gulimall-prod-eks`
- Spot node group (default **2× t3.large** in `terraform.tfvars.example`)
- **12 ECR repositories** (all microservices + renren-fast)
- **RDS SG rule**: EKS node SG → PostgreSQL 5432 (pods reach RDS inside VPC; no laptop IP)

```bash
cd infra/terraform/environments/prod
# terraform.tfvars: enable_eks=true, rds_security_group_id=sg-...
terraform apply -var="enable_eks=true"
```

Outputs: `eks_cluster_name`, `eks_node_security_group_id`, `ecr_repository_urls`, `certificate_arn_in_use`.

### 2. kubectl + ALB Controller

```bash
aws eks update-kubeconfig --region us-west-2 --name gulimall-prod-eks
./k8s/scripts/eks-install-alb-controller.sh
```

### 3. Push all images

```bash
./k8s/scripts/ecr-push-all.sh
```

### 4. Secrets + Helm

```bash
./k8s/scripts/k8s-create-secrets.sh
# Or use eks-up.sh which sets HELM_CERTIFICATE_ARN from Terraform automatically.
helm upgrade --install gulimall k8s/helm/gulimall \
  -f k8s/helm/gulimall/values.yaml \
  -f k8s/helm/gulimall/values-eks.yaml \
  --namespace gulimall --create-namespace --wait --timeout 45m
```

`eks-up.sh` runs the above automatically.

### 5. Verify

```bash
kubectl get pods -n gulimall
kubectl get ingress -n gulimall
curl -s https://www.yangzhangtech.online/actuator/health
```

**Mall UI** is not in the cluster — deploy `gulimall-mall` separately (Amplify/EC2) with `NEXT_PUBLIC_API_BASE=https://www.yangzhangtech.online`.

**Admin UI** — `renren-fast-vue` with API proxy to the same origin.

### 6. Save money when idle

```bash
./k8s/scripts/eks-down.sh scale    # nodes → 0
./k8s/scripts/eks-down.sh destroy  # remove EKS module
```

---

## Helm overlays

| File | Purpose |
|------|---------|
| `values.yaml` | Base chart (all apps enabled) |
| `values-kind.yaml` | kind: ES + renren, seckill off, small JVM |
| `values-eks.yaml` | ECR images, ALB Ingress, prod cookies, HPA (gateway/product), seckill off |

---

## RDS on EKS vs kind

- **kind** on your Mac: JDBC uses **your public IP** in RDS security group (changes when you switch Wi‑Fi).
- **EKS**: pods reach RDS via the **EKS-managed cluster security group** on worker nodes (see `eks-rds.tf` → `cluster_security_group_id`). The custom Terraform `node-sg` is not attached to managed node ENIs.

## Save money when idle

```bash
./k8s/scripts/eks-down.sh scale    # nodes → 0 (keeps ~$73/mo control plane)
./k8s/scripts/eks-down.sh destroy  # remove EKS module entirely
```

Do **not** run `scale` while you are actively using the cluster.

## Admin demo account (maintainers)

To provision or reset the read-only `demo` reviewer account on RDS (`ecommerce_admin`):

```bash
./scripts/apply-admin-demo-user.sh   # requires .env with RDS_*
```

---

## Related

- `docs/CD.md` — GitHub Actions → ECR → EKS, secrets, rollback
- `docs/load-test.md` — k6 scenarios, HPA verification, recorded RPS
- `infra/terraform/README.md` — ALB, WAF, Route53
- `docs/production-resilience-checklist.md`
