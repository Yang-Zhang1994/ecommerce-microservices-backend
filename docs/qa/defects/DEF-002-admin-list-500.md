# DEF-002 — Admin product list returned HTTP 500 (Druid Wall / JPA)

**Status:** Closed
**Severity:** High
**Area:** admin / renren-fast

## Repro steps
1. Open admin console product (or attr) list pages against a live stack.
2. Trigger list query that uses JPA-style select patterns Druid Wall rejects.
3. Observe API 500 and empty UI.

## Expected
List endpoints return 200 + page data (or empty page), UI renders rows.

## Actual
HTTP 500 from Druid Wall filter rejecting legitimate select patterns (`select-always-true` class failures).

## Fix PR
Disable Druid Wall filter for admin list paths (commit `9b7c4be` lineage); related startup hardening for Quartz/captcha when RDS slow (`e61f779`, `1bd29ff`).

## Re-verify test
- [x] Manual: admin list pages load after fix on kind/EKS
- [x] Automated: CI build/deploy path no longer fails on renren-fast boot for wall-related 500s
- [x] Result: PASS (verified in deploy follow-ups)
