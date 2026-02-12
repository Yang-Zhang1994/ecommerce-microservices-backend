# S3 桶配置（解决上传 403）

## 1. CORS（必配，否则浏览器 PUT 会 403）

在 AWS 控制台：S3 → 桶 `ecommerce-uploads-2` → 权限 → **CORS**，配置如下：

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "HEAD"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag"]
  }
]
```

生产环境可将 `AllowedOrigins` 改为你的前端域名，例如 `["https://your-domain.com"]`。

## 2. IAM 权限

生成预签名 URL 使用的 AWS 凭证（application-local.yml 中的 access-key）对应的 IAM 用户，必须对桶有 **s3:PutObject** 权限。例如桶策略或用户策略：

```json
{
  "Effect": "Allow",
  "Action": ["s3:PutObject"],
  "Resource": "arn:aws:s3:::ecommerce-uploads-2/*"
}
```

## 3. 公网可读（必配，否则上传后图片回显不出来）

前端用 `<img src="S3的URL">` 展示图片时，浏览器会直接请求该 URL；若对象为私有，S3 返回 403，图片无法显示。因此需在桶 **权限** 中添加上传对象的公开读策略，例如：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::ecommerce-uploads-2/*"
    }
  ]
}
```

---

**403 排查顺序**：先确认 CORS 已按上面配置并保存，再确认 IAM 有 s3:PutObject；前端重试上传后，若仍 403，看浏览器报错里 S3 返回的正文（如 SignatureDoesNotMatch、AccessDenied 等）再对症处理。
