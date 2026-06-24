# Production resilience checklist (completed items)

## Done in AWS / repo

1. **EC2**: `redis` + `consul` + `gulimall-gateway` running; gateway JAR rebuilt with default `/api` **RequestRateLimiter** (see `X-RateLimit-*` response headers).
2. **Gateway**: `default-filters` rate limit + order/auth/search/cart circuit breakers; compose env `GATEWAY_RATELIMIT_*`.
3. **WAF**: `ecommerce-api-wide-ratelimit` — **3000 req / 5 min / IP** for paths starting with `/api/`.
4. **CloudWatch alarms** (prefix `gulimall-prod-`):
   - `gulimall-prod-alb-target-5xx`
   - `gulimall-prod-alb-elb-5xx`
   - `gulimall-prod-tg-unhealthy-hosts`
   - `gulimall-prod-waf-blocked-requests`
5. **ACM**: certificate requested for `www.ecommerce.com` + `ecommerce.com` — **add DNS CNAMEs** from `terraform output acm_validation_records`, then set `certificate_arn` in `terraform.tfvars` and `terraform apply` for **HTTPS:443**.
6. **Scripts**: `scripts/deploy-prod-ec2-gateway.sh`, `scripts/verify-gateway-ratelimit.sh`, `scripts/loadtest-alb.sh`.

## HTTPS (manual DNS step)

```bash
cd infra/terraform/environments/prod
terraform output acm_validation_records
```

Add the two **CNAME** records at your DNS host. When ACM status is **ISSUED**:

```bash
# terraform.tfvars
certificate_arn = "arn:aws:acm:us-west-2:466297333400:certificate/1e667b05-62fc-4df9-abcc-2c18a849099c"
terraform apply
```

## Verify rate limit (429)

Auth/order routes return **503** when `gulimall-auth-server` is down (no Consul registration), but rate limiting is active if responses include:

`X-RateLimit-Remaining`, `X-RateLimit-Burst-Capacity`, etc.

To see **429**, lower limits and burst in parallel:

```bash
export GATEWAY_RATELIMIT_AUTH_REPLENISH=2 GATEWAY_RATELIMIT_AUTH_BURST=5
export GATEWAY_RATELIMIT_API_REPLENISH=2 GATEWAY_RATELIMIT_API_BURST=5
# redeploy gateway, then on EC2:
for i in $(seq 1 50); do curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:88/api/auth/ping & done; wait
```

## Load test

```bash
brew install hey
./scripts/loadtest-alb.sh http://ecommerce-prod-alb-....elb.amazonaws.com 20 30s
```
