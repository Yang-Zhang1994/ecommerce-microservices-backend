# AWS CLI 采集结果（prod / us-west-2）

采集时间：由 `admin` 用户在账号 **466297333400** 执行。

## 汇总

| 资源 | 值 |
|------|-----|
| ALB 名称 | `ecommerce-prod-alb` |
| ALB ARN | `arn:aws:elasticloadbalancing:us-west-2:466297333400:loadbalancer/app/ecommerce-prod-alb/b205eff04a246026` |
| ALB DNS | `ecommerce-prod-alb-1794368425.us-west-2.elb.amazonaws.com` |
| ALB 状态 | `active` |
| ALB 安全组 | `sg-01bcc71a8b21860d1`（`gulimall-alb-sg`，入站 80/443 0.0.0.0/0） |
| VPC | `vpc-014eddd3289d3be1d` |
| 子网 | `subnet-01ab2b35b0835c874` (2b), `subnet-069bc46fb157dce5c` (2a) |
| Target Group | `ecommerce-gateway-tg` |
| TG ARN | `arn:aws:elasticloadbalancing:us-west-2:466297333400:targetgroup/ecommerce-gateway-tg/e9a1878d6f471c8a` |
| TG 端口 / 健康检查 | **88**，`/actuator/health`，matcher **200**，healthy_threshold **5** |
| TG 已注册目标 | **0**（尚无 healthy 实例） |
| Listener | 仅 **HTTP:80** → forward TG |
| Listener ARN | `arn:aws:elasticloadbalancing:us-west-2:466297333400:listener/app/ecommerce-prod-alb/b205eff04a246026/fea0b83cd52d63c7` |
| HTTPS / ACM | **无** |
| WAF 名称 | `ecommerce-prod-waf` |
| WAF ID | `8808d7cc-130b-44f3-89db-8e2c4f84e44f` |
| WAF ARN | `arn:aws:wafv2:us-west-2:466297333400:regional/webacl/ecommerce-prod-waf/8808d7cc-130b-44f3-89db-8e2c4f84e44f` |
| WAF ↔ ALB 关联 | **尚未关联**（`list-resources-for-web-acl` 为空） |
| WAF Capacity | 931 WCU |

## WAF 规则（与 Terraform 模块已对齐）

| Priority | 名称 | 行为 |
|----------|------|------|
| 0 | AWSManagedRulesAmazonIpReputationList | Block (override none) |
| 1 | ecommerce-auth-webhook-ratelimit | 1000 req / **300s** / IP；路径 `/api/auth` **或** `/api/order/pay/stripe/webhook`；**Block 429** |
| 2 | AWSManagedRulesCommonRuleSet | Block |
| 3 | AWSManagedRulesKnownBadInputsRuleSet | Block |

## 待你本地完成

1. **EC2 网关** 启动并监听 **88**，安全组放行 **88 ← sg-01bcc71a8b21860d1**（`ecommerce-ec2-sg` 等）。
2. **Target Group** 注册实例（控制台或 `target_instance_ids`）。
3. `cd environments/prod && cp terraform.tfvars.example terraform.tfvars`
4. `terraform init` → `terraform plan`（含 `import.tf`）→ `apply`（会创建 **WAF↔ALB 关联**）。

## 复现 CLI

```bash
export AWS_REGION=us-west-2
ALB_ARN="arn:aws:elasticloadbalancing:us-west-2:466297333400:loadbalancer/app/ecommerce-prod-alb/b205eff04a246026"
aws elbv2 describe-target-groups --load-balancer-arn "$ALB_ARN" --region $AWS_REGION
aws elbv2 describe-listeners --load-balancer-arn "$ALB_ARN" --region $AWS_REGION
aws wafv2 get-web-acl --name ecommerce-prod-waf --scope REGIONAL \
  --id 8808d7cc-130b-44f3-89db-8e2c4f84e44f --region $AWS_REGION
```
