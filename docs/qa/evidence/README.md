# QA evidence (checkout-path smoke)

Screenshots from the UI-contract fixture (same assertions as Playwright + Selenium):

| File | Case |
|------|------|
| `tc01-empty-login.png` | TC-01 empty Sign in validation |
| `tc02-cart.png` | TC-02 My Cart heading |

Regenerate:

```bash
bash scripts/qa/serve-checkout-fixture.sh &
PLAYWRIGHT_CHANNEL=chrome PLAYWRIGHT_BASE_URL=http://127.0.0.1:4173 \
  npx --prefix gulimall-mall playwright test e2e/checkout-path.smoke.spec.ts
```
