# AWS 前端栈重建说明 (us-west-2 / account 466297333400)

> 本目录是 2026-06-01 删除 ALB + WAF + EIP 前导出的配置存档。
> 想恢复时，把这份文件发给 AI 说"照 REBUILD.md 装回来"即可。
> 同目录的 `*.json` 是原始导出，本文件是精简版重建参数。

## 删除了什么（激进全删方案）

- ALB `ecommerce-prod-alb`（含 HTTP/HTTPS 监听器、目标组 `ecommerce-gateway-tg`）
- WAF `ecommerce-prod-waf`（Web ACL + 5 条规则）
- 弹性 IP：ALB 的 2 个 + 停机 EC2 的 1 个

## 保留未动（重建会用到）

- **EC2 实例**：`i-05b954f202863481b`（Ecommerce-Server, t3.medium, 停机中）
- **ACM 证书**：`arn:aws:acm:us-west-2:466297333400:certificate/396a89d4-4a41-431a-bacc-52b565d4665c`（覆盖 yangzhangtech.online + www，未删，可复用）
- **安全组**：`sg-01bcc71a8b21860d1` (gulimall-alb-sg)，入站放行 80/443 from 0.0.0.0/0（未删）
- **RDS 及其弹性 IP** `44.253.109.12`（未动）
- **Route53 托管区**：`Z027782825ILOVMTMOM8G` (yangzhangtech.online)
- **VPC**：`vpc-014eddd3289d3be1d`

## 重建参数

### ALB
- 类型：application，scheme=internet-facing，ipv4
- 子网：`subnet-01ab2b35b0835c874` (us-west-2b) + `subnet-069bc46fb157dce5c` (us-west-2a)
- 安全组：`sg-01bcc71a8b21860d1`

### 目标组 ecommerce-gateway-tg
- target-type=instance，protocol=HTTP，port=**88**，vpc=`vpc-014eddd3289d3be1d`
- 健康检查：HTTP，path=`/actuator/health`，port=traffic-port
- 注册目标：实例 `i-05b954f202863481b`

### 监听器
- :80 HTTP → 默认动作 redirect 到 HTTPS:443，HTTP_301
- :443 HTTPS → forward 到目标组；证书 ARN 见上方 ACM

### WAF (ecommerce-prod-waf, REGIONAL)
- 完整规则见 `waf-web-acl.json`，5 条：
  - AWS-AWSManagedRulesCommonRuleSet
  - AWS-AWSManagedRulesKnownBadInputsRuleSet
  - AWS-AWSManagedRulesAmazonIpReputationList
  - ecommerce-auth-webhook-ratelimit（自定义限流）
  - ecommerce-api-wide-ratelimit（自定义限流）
- 建好后关联到新 ALB

### Route53（重建后需更新）
重建 ALB 后 DNS 名会变，需把这两条 A 别名记录指向新 ALB：
- `yangzhangtech.online` (A, alias)
- `www.yangzhangtech.online` (A, alias)
（原指向：ecommerce-prod-alb-1794368425.us-west-2.elb.amazonaws.com）
