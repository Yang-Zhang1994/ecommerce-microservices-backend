# Continuous delivery — GitHub Actions → ECR → EKS

Push to `main` runs:

```text
unit tests (9 JUnit) → Maven package → Docker build (12 images) → ECR push (git SHA + latest)
  → helm upgrade --atomic (global.imageTag = SHA)
```

Workflows:

| File | Trigger | Purpose |
|------|---------|---------|
| `.github/workflows/ci.yml` | PR / push (Java paths) | 9 unit tests gate |
| `.github/workflows/cd-eks.yml` | push `main` / manual | Full CD to EKS |

Scripts (called by Actions):

| Script | Role |
|--------|------|
| `k8s/scripts/gh-actions-build-push.sh` | Build + push 12 ECR images |
| `k8s/scripts/gh-actions-helm-deploy.sh` | `helm upgrade --atomic` with `global.imageTag` |
| `k8s/scripts/gh-actions-sync-secrets.sh` | Apply `gulimall-secrets` from GitHub Secrets (manual only) |

## One-time setup

### 1. Terraform — GitHub OIDC IAM role

```bash
cd infra/terraform/environments/prod
terraform apply \
  -var="enable_eks=true" \
  -var="enable_github_actions_cd=true" \
  -var="github_repository=Yang-Zhang1994/ecommerce-microservices-backend" \
  -var="rds_security_group_id=sg-0b90bf2ba9a126496"
```

If OIDC provider already exists in the account, import or remove duplicate:

```bash
terraform import 'aws_iam_openid_connect_provider.github_actions[0]' \
  arn:aws:iam::466297333400:oidc-provider/token.actions.githubusercontent.com
```

Output:

```bash
terraform output -raw github_actions_cd_role_arn
```

### 2. GitHub repository secrets

**Required (CD):**

| Secret | Value |
|--------|--------|
| `AWS_ROLE_TO_ASSUME` | `terraform output -raw github_actions_cd_role_arn` |
| `HELM_CERTIFICATE_ARN` | ACM cert for `www.yangzhangtech.online` (terraform `certificate_arn_in_use`) |

**Only for “Sync secrets” manual workflow** (store app credentials — never commit):

| Secret | Maps to |
|--------|---------|
| `RDS_ENDPOINT` | PostgreSQL host |
| `RDS_USERNAME` | DB user |
| `RDS_PASSWORD` | DB password |
| `ORDER_STRIPE_SECRET_KEY` | Stripe secret |
| `ORDER_STRIPE_WEBHOOK_SECRET` | Stripe webhook |
| `GOOGLE_CLIENT_ID` | OAuth |
| `GOOGLE_CLIENT_SECRET` | OAuth |
| `AWS_ACCESS_KEY_ID` | S3 third-party (optional) |
| `AWS_SECRET_ACCESS_KEY` | S3 third-party (optional) |

Normal CD **does not** recreate Kubernetes secrets — they stay in the cluster. Use **Actions → CD — EKS → Run workflow → Sync secrets** once after first deploy or when rotating credentials.

### 3. Cluster must be running

CD does not run Terraform or scale nodes. Before first CD:

```bash
./k8s/scripts/eks-up.sh   # or scale node group to desired_size ≥ 1
kubectl get nodes
```

## Day-to-day

- **Merge to `main`** → CD runs automatically (~45–90 min first time; mostly Docker builds).
- **Manual redeploy** (same images): Actions → CD — EKS → `skip_build: true` → optional `image_tag`.
- **Rotate RDS/Stripe**: Run workflow with `sync_secrets: true` (does not rebuild images).

## Rollback (interview talking points)

Helm keeps release history. Every deploy uses **`--atomic`**: if pods fail readiness, Helm auto-rolls back the release.

```bash
aws eks update-kubeconfig --region us-west-2 --name gulimall-prod-eks
helm history gulimall -n gulimall
helm rollback gulimall <revision> -n gulimall   # e.g. previous revision
```

**Image-level rollback:** redeploy an older git SHA:

```bash
# GitHub Actions → CD — EKS → skip_build=true, image_tag=<previous-sha>
# Or locally:
IMAGE_TAG=abc1234 HELM_CERTIFICATE_ARN=arn:aws:acm:... ./k8s/scripts/gh-actions-helm-deploy.sh
```

**What we do not rollback automatically:** database migrations (none in CD), Elasticsearch index, or Kubernetes Secrets.

## Security model

| Concern | Approach |
|---------|----------|
| AWS credentials | **OIDC** (`sts:AssumeRoleWithWebIdentity`) — no access keys in repo |
| App secrets (RDS, Stripe) | **GitHub Secrets** → `kubectl apply` secret; not in Helm values |
| Image immutability | ECR tag = **git SHA**; `latest` also updated for convenience |
| Blast radius | IAM policy scoped to **12 ECR repos** + **one EKS cluster** |
| Least privilege (future) | Replace `AmazonEKSClusterAdminPolicy` with namespace-scoped RBAC |

## Resume / interview one-liner

> GitHub Actions on merge to main: unit tests → build 12 Spring Boot images → ECR → Helm atomic deploy on EKS with SHA-tagged rollbacks; app secrets in Kubernetes via GitHub Secrets, not in the chart.

## Troubleshooting

| Symptom | Check |
|---------|--------|
| `Not authorized to perform sts:AssumeRoleWithWebIdentity` | `AWS_ROLE_TO_ASSUME` secret; repo name matches `github_repository` in Terraform |
| `helm upgrade` timeout | Nodes scaled to 0; RDS stopped; pods Pending |
| `Unauthorized` kubectl | EKS access entry not applied — re-run terraform with `enable_github_actions_cd=true` |
| Build > 90 min | Normal on free tier; consider path filters or matrix builds later |

See also [EKS.md](EKS.md), [load-test.md](load-test.md).
