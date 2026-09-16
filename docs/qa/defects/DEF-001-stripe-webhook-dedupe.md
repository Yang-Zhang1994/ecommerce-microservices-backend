# DEF-001 — Stripe webhook duplicate `eventId` could re-process payment

**Status:** Closed
**Severity:** High
**Area:** order / payment

## Repro steps
1. Receive a valid Stripe webhook for `checkout.session.completed` with `eventId=evt_X`.
2. Replay the same payload + signature (Stripe retry or manual replay).
3. Observe order/payment side effects.

## Expected
Second delivery with the same `eventId` is skipped; order state unchanged; audit/log notes duplicate.

## Actual
Without dedupe, handlers could treat retries as new work (double side effects risk).

## Fix PR
Covered by idempotent webhook store + unit gate: `StripePaymentServiceImplTest` (duplicate `evt_duplicate_123` never calls `saveReceived` again). Related order idempotency: `IdempotencyServiceTest`.

## Re-verify test
- [x] Automated: `mvn -pl gulimall-order -am test -Dtest=StripePaymentServiceImplTest,IdempotencyServiceTest`
- [x] Result: PASS
