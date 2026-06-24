# 将 DNS 从 Cloudflare 迁到 Route53（全 AWS）

目标：**域名解析、ACM 验证、www/apex → ALB** 全部由 **Terraform + Route53** 管理，不再在 Cloudflare 手工加 CNAME。

## 架构变化

```text
之前: 用户 → Cloudflare DNS → (手工 CNAME) → ALB
之后: 用户 → Route53 → ALB (Alias A) + ACM 在 AWS 自动验证
```

迁走后 **Cloudflare 橙云 CDN/WAF 不再挡在 DNS 层**（入口改为 **AWS WAF + ALB**，你项目里已配置）。若仍要 Cloudflare CDN，应改用 **CNAME 接入** 而不是改 NS，那是另一条路线。

## 分三步做（降低宕机风险）

### 第 1 步：只创建 Route53 托管区

`infra/terraform/environments/prod/terraform.tfvars`：

```hcl
manage_dns_in_route53 = true
request_acm_certificate = true
certificate_arn = ""   # 让 ACM 模块继续管理证书
```

```bash
cd infra/terraform/environments/prod
terraform init
terraform apply -target=module.dns_zone[0]
terraform output route53_name_servers
```

记下 **4 条 NS**，形如：

`ns-xxx.awsdns-xx.org` 等。

### 第 2 步：改 NS（在域名注册商）

**ecommerce.com** 的注册商是 **Tucows**（常见于 **Hover**、部分 Namecheap 代理），NS 当前指向 Cloudflare。必须在 **注册商后台**改 NS（不是 Cloudflare DNS 页面里加记录）。

| 注册商入口 | 操作 |
|------------|------|
| **[Hover](https://www.hover.com/control_panel/domain)** | 域名 → **DNS** → **Nameservers** → **Edit** → **Custom nameservers** → 填入下面 4 条 AWS NS → Save |
| 其他 Tucows 面板 | 找 **Nameservers / Custom DNS** → 替换 Cloudflare 两条为 Route53 四条 |
| 域名在 **Route53 Domains** | AWS Console → Route53 → **Registered domains** → 改 name servers |

Terraform 已预置 **Google MX / SPF / site verification / mail A**（`modules/route53-preserved`），避免切 NS 后邮件中断。原 **www/apex → Railway** 会改为 **→ ALB**（`modules/route53-alias`）。

建议：

- 迁移前把 Cloudflare 里现有 **A/CNAME/TXT** 记录**抄一份**（邮件 MX、Google 验证等），必要时在 Route53 **手动补**同名记录。
- TTL 可先改短（300s）再切 NS，缩短生效时间。
- 全球传播通常 **15 分钟～48 小时**；可用 `dig NS ecommerce.com` 检查是否已是 `awsdns`。

### 第 3 步：全量 apply（验证证书 + HTTPS + 指向 ALB）

NS 生效后：

```bash
cd infra/terraform/environments/prod
terraform apply
```

Terraform 将自动：

1. 在 Route53 创建 **ACM 验证 CNAME**（`modules/acm`）
2. 等待 **`aws_acm_certificate_validation`** → 证书 **ISSUED**
3. 为 ALB 创建 **HTTPS:443** 监听器（`certificate_arn` 自动注入）
4. 创建 **`www.ecommerce.com`** 与 **`ecommerce.com`** 的 **Alias A** → ALB（`modules/route53-alias`）

验证：

```bash
dig NS ecommerce.com +short
dig www.ecommerce.com +short
curl -sI https://www.ecommerce.com/actuator/health
```

## 与现有 ACM 证书的关系

账号里已有证书  
`arn:aws:acm:us-west-2:466297333400:certificate/1e667b05-62fc-4df9-abcc-2c18a849099c`（可能仍为 PENDING）。

启用 `manage_dns_in_route53 = true` 后，Terraform 会在 **Route53** 写入验证记录；**NS 切到 Route53 后**，同一张证书通常会变为 **ISSUED**，无需在 Cloudflare 再加验证 CNAME。

若希望 **全新证书**，可先 `terraform destroy` 仅 `module.acm`（谨慎），再 `apply`；一般 **不必**，沿用现有即可。

## 回滚

- 把注册商 NS **改回 Cloudflare** 的 `tegan.ns.cloudflare.com` / `anton.ns.cloudflare.com`。
- `manage_dns_in_route53 = false`，`terraform apply`（不会删 Zone，除非 `terraform destroy` 指定模块；Hosted Zone 默认 `force_destroy = false`）。

## 相关 Terraform 模块

| 模块 | 作用 |
|------|------|
| `modules/route53-zone` | 托管区 + 输出 NS |
| `modules/acm` | 证书 + Route53 验证 + `certificate_validation` |
| `modules/route53-alias` | www/apex → ALB |

Cloudflare 手工验证说明（旧路径）：[acm-dns-cloudflare.md](./acm-dns-cloudflare.md)
