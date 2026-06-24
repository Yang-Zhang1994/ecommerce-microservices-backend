# Gulimall — Terraform (ALB + WAF, import-first)

Codifies **Application Load Balancer**, **Target Group** (`HTTP:88`, `/actuator/health`), and **Regional WAF** (managed rule groups + **rate limit `/api/auth`**: 1000 req / 5 min / IP).

## Layout

```text
infra/terraform/
├── README.md
├── backend.tf.example          # S3 remote state template
├── modules/
│   ├── alb/
│   ├── eks/                    # optional EKS + Spot node group (enable_eks)
│   ├── waf/
│   └── security-groups/        # optional; import path uses existing SGs
├── environments/dev/
├── environments/prod/          # CLI-filled import for ecommerce-prod-* (see prod/COLLECTED.md)
│   └── manage_dns_in_route53   # Route53 zone + ACM auto-validate + ALB aliases
    ├── main.tf
    ├── terraform.tfvars.example
    ├── import.tf.example
    └── backend.local.tf.example
```

## Prerequisites

- Terraform **>= 1.5**
- AWS CLI credentials (`aws sts get-caller-identity`)
- Existing resources in **us-west-2** (or change `aws_region`): ALB, target group, Web ACL, ALB security group(s)

## Quick start (import path)

### 1. Configure variables

```bash
cd infra/terraform/environments/dev
cp terraform.tfvars.example terraform.tfvars
# Edit: account_id, vpc_id, subnets, alb_name, waf_web_acl_name, alb_security_group_ids
```

### 2. Backend (pick one)

**Local state (first plan only):**

```bash
cp backend.local.tf.example backend.override.tf
terraform init -reconfigure
```

**S3 remote state (team):**

```bash
# Create bucket + DynamoDB lock table once, then:
cp ../../backend.tf.example backend.tf   # fix ACCOUNT_ID
terraform init
```

### 3. Import existing resources

**Option A — `import` blocks (Terraform 1.5+)**

```bash
cp import.tf.example import.tf
# Fill ARNs/IDs from console (comments in file)
terraform plan   # shows import plan
terraform apply  # imports into state
```

**Option B — CLI**

```bash
terraform init

# ALB
terraform import 'module.alb.aws_lb.this' \
  'arn:aws:elasticloadbalancing:us-west-2:ACCOUNT:loadbalancer/app/NAME/ID'

# Target group
terraform import 'module.alb.aws_lb_target_group.gateway' \
  'arn:aws:elasticloadbalancing:us-west-2:ACCOUNT:targetgroup/ecommerce-gateway-tg/TGID'

# Listener(s) — one per listener ARN
terraform import 'module.alb.aws_lb_listener.https[0]' 'arn:aws:elasticloadbalancing:...:listener/app/...'

# WAF ACL (id = name/id/REGIONAL from WAF console)
terraform import 'module.waf.aws_wafv2_web_acl.this' 'my-acl-name/acl-uuid/REGIONAL'

# WAF ↔ ALB association (id = web_acl_arn,alb_arn)
terraform import 'module.waf.aws_wafv2_web_acl_association.alb' \
  'arn:aws:wafv2:us-west-2:ACCOUNT:regional/webacl/.../...,arn:aws:elasticloadbalancing:...:loadbalancer/app/...'
```

### 4. Align until `plan` is clean

```bash
terraform plan
```

- Tune `certificate_arn`, `waf_managed_rules_action` (`count` vs `none`), listener counts in code vs console.
- After import, remove `import.tf` or comment blocks to avoid re-import.

### 5. Outputs

```bash
terraform output alb_dns_name
terraform output waf_web_acl_arn
```

## WAF behaviour (module defaults)

| Rule | Default |
|------|---------|
| `AWSManagedRulesCommonRuleSet` | `count` (observe) — set `waf_managed_rules_action = "none"` to block |
| `AWSManagedRulesKnownBadInputsRuleSet` | same |
| `rate-limit-api-auth` | **1000** / 5 min / IP, path **starts with** `/api/auth`, action **block** |

Matches gateway route `/api/auth/**` and console drill.

## Security groups

- **`create_security_groups = true`**: creates `*-alb-sg` (443/80) and `*-app-sg` (88 from ALB only).
- **Import path (default)**: `create_security_groups = false`, set `alb_security_group_ids` to ALB’s SG. Keep **EC2** rules (e.g. `ecommerce-ec2-sg`: **88 from ALB SG**) in console or extend Terraform later.

## CI (sketch)

```yaml
# PR: terraform fmt -check && terraform init -backend=false && terraform validate
# main: terraform plan/apply with OIDC role + S3 backend
```

## EKS (optional)

Set `enable_eks = true` in `environments/prod/terraform.tfvars`. Creates cluster `gulimall-prod-eks`, single Spot `t3.medium` node group, and ECR repos for gateway/order/ware. Allow `eks_node_security_group_id` on RDS. Deploy: [docs/EKS.md](../../docs/EKS.md).

## Related docs

- [docs/EKS.md](../../docs/EKS.md) — kind + Helm + EKS
- [docs/resilience-and-traffic.md](../../docs/resilience-and-traffic.md) — gateway Redis limit + WAF layering
