# GrainMart — QA test plan (purchase path)

Scope: browse → login → cart → checkout / payment integrity.
Environments: local (`kind`), AWS EKS (when up), UI-contract fixture for CI driver smoke.

| Case ID | Steps | Expected | Automated? |
|--------|--------|----------|------------|
| TC-01 | Open `/login` → submit empty username/password | Error: enter username and password; stay on Sign in | Playwright + Selenium smoke; Manual exploratory |
| TC-02 | Open `/` → Cart → `/cart/list` (guest) | Brand visible; My Cart (or login redirect if auth gate) | Playwright + Selenium smoke; Manual |
| TC-03 | Guest tries checkout without session | Redirect / prompt to Sign in; no Stripe session created | Manual + API script `k8s/scripts/e2e-order-a1234.sh` (auth required) |
| TC-04 | Authenticated: login → cart → submit → Stripe Checkout | Hosted Checkout URL; pay 4242 → `/order/success` | Playwright `stripe-checkout-once` (needs `CHECKOUT_URL`); Manual |
| TC-05 | Stripe webhook replay (same `eventId`) | Duplicate skipped; order state unchanged | JUnit `StripePaymentServiceImplTest`; Manual with Stripe CLI |

**Unit / contract gates (CI):** `ProtectedCacheTest`, `IdempotencyServiceTest`, `OrderWorkflowServiceImplTest`, `StripePaymentServiceImplTest` via GitHub Actions and `azure-pipelines.yml`.

**Defect process:** use [defect-template.md](qa/defect-template.md); closed examples in [qa/defects/](qa/defects/).

**ADO Test Cases:** [qa/ado-test-cases.md](qa/ado-test-cases.md) (TC-01…TC-03 mapped for Azure Test Plans).
