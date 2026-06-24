# Namecheap → Route53（yangzhangtech.online）

域名在 **Namecheap** 购买时，在注册商改 NS（不是 Cloudflare）。

## 1. 拿到 Route53 的 4 条 NS

```bash
cd infra/terraform/environments/prod
terraform output route53_name_servers
```

## 2. Namecheap 操作

1. 登录 [Namecheap](https://www.namecheap.com/) → **Dashboard**
2. 在 **yangzhangtech.online** 一行点 **MANAGE**
3. 左侧或顶部进入 **Domain** → **Nameservers**
4. 选择 **Custom DNS**（不要再用 Namecheap BasicDNS）
5. 填入 Terraform 输出的 **4 条** `ns-*.awsdns-*.xx`
6. 保存（绿色勾）

## 3. 等传播后

```bash
dig NS yangzhangtech.online +short
./scripts/route53-ns-cutover-check.sh yangzhangtech.online
cd infra/terraform/environments/prod && terraform apply
```

## 4. 访问

- `https://www.yangzhangtech.online` → ALB / 网关
- 开发期 `.env` 示例见 `.env.example`（`GULIMALL_MALL_PUBLIC_ORIGIN` 等）
