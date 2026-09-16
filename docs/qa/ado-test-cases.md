# Azure DevOps — Test Cases (import / mirror)

**Status:** Spec + bootstrap script in repo. Live work items: run `scripts/qa/ado-bootstrap.py` (see [ado-setup.md](ado-setup.md)).

Use these three cases in a free Azure DevOps project → **Test Plans** → Test Cases.
Same IDs as [qa-test-plan.md](../qa-test-plan.md). Pipeline YAML: repo root `azure-pipelines.yml`.

**Automated import:**

```bash
export AZURE_DEVOPS_EXT_PAT='...'
python3 scripts/qa/ado-bootstrap.py --org YOUR_ORG --project GrainMart-QA
```

## TC-01 — Login empty submit (negative)

| Field | Value |
|-------|--------|
| Title | Login: empty credentials show validation error |
| Steps | 1. Open `/login` 2. Leave fields empty 3. Click Sign in |
| Expected | Error text about username/password; remain on Sign in |
| Automation | Playwright + Selenium `checkout-path` smoke (fixture or live) |

## TC-02 — Guest cart path

| Field | Value |
|-------|--------|
| Title | Guest can open home and cart |
| Steps | 1. Open `/` 2. Click Cart / open `/cart/list` |
| Expected | Brand **GrainMart** on home; **My Cart** heading on cart (fixture) or live cart UI |
| Automation | Playwright + Selenium smoke |

## TC-03 — Checkout requires auth (failure path)

| Field | Value |
|-------|--------|
| Title | Checkout without session does not create Stripe session |
| Steps | 1. Clear cookies 2. Attempt order confirm / pay entry as guest |
| Expected | Sign-in required; no successful Stripe Checkout session |
| Automation | Manual + API purchase script (auth); exploratory on live mall |

### Wire-up (once)

1. Create free org at https://dev.azure.com → new project `GrainMart-QA`.
2. **Pipelines** → New pipeline → GitHub → this repo → existing `azure-pipelines.yml`.
3. **Test Plans** → create suite “Purchase path” → add TC-01…TC-03 from this doc.
4. Link pipeline run to the suite (optional); keep GitHub Actions as the primary merge gate if preferred.
