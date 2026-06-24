# ACM 证书 DNS 验证（Cloudflare）

域名 **ecommerce.com** 的 NS 在 **Cloudflare**（`tegan.ns.cloudflare.com` / `anton.ns.cloudflare.com`），需在 Cloudflare DNS 添加以下 **CNAME**（仅用于证书签发，可验证后删除）。

| 类型 | 名称（Cloudflare「Name」列填） | 目标（Target / Content） |
|------|-------------------------------|---------------------------|
| CNAME | `_320a2840bc45291f2893dd5da0932d78` | `_f5b5ffde3b3f170bf8a5a4c6431b4e5e.jkddzztszm.acm-validations.aws` |
| CNAME | `_25924f2fffd96533d4f5b69ef1d8eea9.www` | `_4bc2f30d019305ff256b394222bddfef.jkddzztszm.acm-validations.aws` |

Cloudflare 上 **Proxy status** 请设为 **DNS only（灰云）**，不要橙色代理，否则 ACM 无法验证。

## 验证证书状态

```bash
aws acm describe-certificate \
  --certificate-arn arn:aws:acm:us-west-2:466297333400:certificate/1e667b05-62fc-4df9-abcc-2c18a849099c \
  --region us-west-2 \
  --query 'Certificate.Status' --output text
```

当输出 **`ISSUED`** 后：

```bash
cd infra/terraform/environments/prod
# terraform.tfvars
certificate_arn = "arn:aws:acm:us-west-2:466297333400:certificate/1e667b05-62fc-4df9-abcc-2c18a849099c"
terraform apply
```

将开启 **ALB HTTPS:443**，HTTP:80 重定向到 443。

## 业务域名指向 ALB（证书签发后）

| 类型 | 名称 | 目标 |
|------|------|------|
| CNAME | `www` | `ecommerce-prod-alb-1794368425.us-west-2.elb.amazonaws.com` |
| CNAME 或 ALIAS | `@`（apex） | 同上（Cloudflare 对 apex 可用 CNAME flattening） |

业务记录也可用橙云代理，但 ACM 验证两条必须灰云。
