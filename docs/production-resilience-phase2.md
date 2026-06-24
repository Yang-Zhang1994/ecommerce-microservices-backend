# Production resilience — Phase 2

## 1. k6 压测基线

```bash
brew install k6
./scripts/run-k6-baseline.sh
BASE_URL=https://www.yangzhangtech.online ./scripts/run-k6-baseline.sh
```

| 阈值 | 目标 |
|------|------|
| 错误率 | < 1% |
| p95 延迟 | < 800ms（health < 500ms） |

详见 [load-test-baseline.md](./load-test-baseline.md)。

## 2. ASG + ElastiCache（Terraform）

`infra/terraform/environments/prod/terraform.tfvars`：

```hcl
enable_app_asg         = true
enable_elasticache     = true
target_instance_ids    = []   # ASG 接管 TG
app_security_group_ids = ["sg-0b90bf2ba9a126496"]
asg_key_name           = "your-key"
```

```bash
cd infra/terraform/environments/prod
terraform apply
terraform output elasticache_redis_primary
```

EC2 `.env` / gateway：

```bash
SPRING_DATA_REDIS_HOST=<elasticache primary endpoint>
```

## 3. 网关：登录 / 下单 / 支付限流 + Fallback

| 路由 | 默认 replenish/burst (per IP/s) |
|------|----------------------------------|
| `/api/auth/login` 等 POST | 15 / 30 |
| `/api/order/pay/**` | 30 / 60 |
| `/api/order/submit` POST | 20 / 40 |

熔断降级：`/fallback/search` 空结果；`/fallback/product` 降级 JSON；其它 **503** 统一 JSON（`degraded: true`）。

环境变量见 `docker-compose.app.yml` 中 `GATEWAY_RATELIMIT_*`。

## 4. 订单：幂等 + Stripe Webhook

**请求头** `Idempotency-Key: <uuid>`（24h 内同 key 返回相同结果）：

- `POST /api/order/submit`
- `POST /api/order/pay/stripe/checkout-session`

**Webhook**：表 `stripe_webhook_event` 按 `event_id` 去重；失败记 `FAILED` 并打点 `stripe.webhook.failed`。

**重放**（需配置密钥）：

```bash
export ORDER_STRIPE_WEBHOOK_REPLAY_SECRET=your-long-secret
curl -X POST "https://www.yangzhangtech.online/api/order/pay/stripe/webhook/replay/evt_xxx" \
  -H "X-Webhook-Replay-Secret: your-long-secret"
```

## 5. 出站 HTTP：Resilience4j（common）

`ProductApi` / `MemberApi` / `WareApi` 经 `ResilientHttpClientFactory` 包装：连接 3s、读 10s、熔断实例 `productApi` / `memberApi` / `wareApi`。

服务 `application.yml` 可增加：

```yaml
spring:
  config:
    import: optional:classpath:application-resilience.yml
```

网关侧已对 product/member/ware 路由加 `CircuitBreaker` + fallback。
