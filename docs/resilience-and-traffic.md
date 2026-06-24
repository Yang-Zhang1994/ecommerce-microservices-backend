# 流量与韧性（替代 Sentinel 的分层方案）

本文描述 **Gulimall** 中与「削峰、限流、熔断、降级」相关的落地方式，便于本地开发、Docker Compose 与后续上云 / K8s 对齐。

## 架构分层

| 层级 | 作用 | 本项目 |
|------|------|--------|
| 边缘 | WAF、Bot、大流量挡在 Java 外 | 生产建议 **Cloudflare** 或 **AWS WAF + ALB/CloudFront**（配置在 DNS/云平台，不在本仓库） |
| API 网关 | 路由级限流、下游熔断 | **`gulimall-gateway`**：`RequestRateLimiter`（Redis）+ `CircuitBreaker`（Resilience4j Reactor） |
| 业务服务 | 依赖调用熔断/超时 | **`gulimall-order`**：`MemberProfileHttpClient` 使用 **Resilience4j** `@CircuitBreaker`；Stripe 使用 `HttpClient` **connect + request 超时** |
| 可选 | 全局限流与观测 | 边缘 + **Micrometer/Prometheus**（未默认开启，可按需加） |

## 网关（已实现）

- **依赖**：`spring-cloud-starter-circuitbreaker-reactor-resilience4j`、`spring-boot-starter-data-redis-reactive`
- **Redis**：`spring.data.redis.host` 默认 `localhost`；Docker 下网关服务已设置 **`SPRING_DATA_REDIS_HOST=redis`** 并 **`depends_on: redis`**
- **路由**：
  - `/api/order/**`：`CircuitBreaker`（实例名 `orderGatewayCb`）+ `RequestRateLimiter`（令牌桶，环境变量可调）
  - `/api/auth/**`：`CircuitBreaker`（`authGatewayCb`）+ `RequestRateLimiter`
- **健康检查**：`/actuator/health`（HTTP 200，供 **ALB Target Group** 使用；`show-details` 关闭）
- **调参环境变量**（可选）：
  - `GATEWAY_RATELIMIT_ORDER_REPLENISH` / `GATEWAY_RATELIMIT_ORDER_BURST`
  - `GATEWAY_RATELIMIT_AUTH_REPLENISH` / `GATEWAY_RATELIMIT_AUTH_BURST`

## 订单服务（已实现）

- **`MemberProfileHttpClient`**：调用会员 `GET .../member/info/{id}`，熔断名 **`memberService`**，配置见 `gulimall-order` 的 `application.yml` → `resilience4j.circuitbreaker.instances.memberService`
- **Stripe**：`StripePaymentServiceImpl` 中 `HttpClient` 连接超时 **10s**、单次请求超时 **45s**（可按环境再调）

## 本地运行注意

1. **网关限流依赖 Redis**：本机只起网关时需同时起 **Redis**（例如 `docker compose up -d redis`），或设置 `SPRING_DATA_REDIS_HOST` 指向可达的 Redis。
2. **全栈 Compose**：`docker-compose.app.yml` 中网关已依赖 **redis**，与 auth/order 等一致。

## 生产 / WAF（文档级）

- **Stripe Webhook**：`POST /api/order/pay/stripe/webhook` — 在 WAF 中避免误拦（放宽 body 规则或调灵敏度）；慎用易变的 Stripe IP 白名单。
- **OAuth**：`/api/auth/**` 与回调域名需在边缘与网关日志中可追踪；`PreserveHostHeader` 已保留。

## Phase 2（已实现）

- **k6 基线**：`scripts/k6/baseline.js`、`docs/load-test-baseline.md`
- **网关**：登录/支付/下单单独限流；product/member/ware 熔断；`/fallback/*` 降级 JSON
- **订单**：`Idempotency-Key`；Stripe webhook 落库 + 重放；出站 HTTP 超时
- **common**：`ProductApi` / `MemberApi` / `WareApi` Resilience4j 包装
- **Terraform（可选）**：`enable_app_asg`、`enable_elasticache` — 见 [production-resilience-phase2.md](./production-resilience-phase2.md)

## 后续可选（P3–P4）

- 可观测：**Micrometer + Prometheus**，统一 **trace id**（`X-Request-Id` / W3C Traceparent）

## 与 K8s 的关系

上 **Kubernetes** 后：本方案不变；将「Nginx/单机反代」换为 **Ingress** 即可，网关仍作为集群内统一入口；**Service Mesh（Istio）** 为可选增强，非必须。

## Terraform（IaC）

ALB、Target Group、WAF、CloudWatch、ACM 见 **[infra/terraform/README.md](../infra/terraform/README.md)**。DNS 全 AWS：**[route53-ns-migration.md](./route53-ns-migration.md)**。运维清单：**[production-resilience-checklist.md](./production-resilience-checklist.md)**。
