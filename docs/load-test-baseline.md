# 压测基线（k6）

## 运行

```bash
brew install k6
./scripts/run-k6-baseline.sh
# 或指定 ALB
BASE_URL=http://ecommerce-prod-alb-1794368425.us-west-2.elb.amazonaws.com ./scripts/run-k6-baseline.sh
```

## 默认场景

| 场景 | 路径 | 目标 RPS | 持续时间 |
|------|------|----------|----------|
| health_smoke | `GET /actuator/health` | 20 | 2m |
| auth_ping | `GET /api/auth/ping` | 10 | 2m |

环境变量：`K6_HEALTH_RPS`、`K6_AUTH_RPS`、`K6_DURATION`、`BASE_URL`。

## 通过标准（阈值，见 `scripts/k6/baseline.js`）

| 指标 | 上限 | 说明 |
|------|------|------|
| `http_req_failed` | **< 1%** | 含 5xx、超时 |
| `http_req_duration` p95 | **< 800ms** | 全场景 |
| health p95 | **< 500ms** | 仅健康检查 |
| auth_ping p95 | **< 1000ms** | 依赖 auth 是否上线 |

未达标时：查 ALB Target 健康、EC2 CPU、RDS 连接、WAF 拦截、网关 Redis。

## 调高强度

```bash
K6_HEALTH_RPS=50 K6_AUTH_RPS=30 K6_DURATION=5m ./scripts/run-k6-baseline.sh
```

建议在 **ASG ≥2 台**、**ElastiCache Redis** 部署后再做高压测试。
