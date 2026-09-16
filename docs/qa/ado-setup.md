# Azure DevOps QA bootstrap — status

## What you already have (in repo)

| Artifact | Location | Live in ADO portal? |
|----------|----------|---------------------|
| 3 test case specs (TC-01…TC-03) | `docs/qa/ado-test-cases.md` | **Spec only** until bootstrap runs |
| Bug DEF-001 / DEF-002 write-ups | `docs/qa/defects/` | **Spec only** |
| Defect work-item template | `docs/qa/defect-template.md` | N/A |
| UI evidence screenshots | `docs/qa/evidence/tc01-empty-login.png`, `tc02-cart.png` | Local files |
| Azure Pipelines QA YAML | `azure-pipelines.yml` | Needs pipeline wired in ADO |
| Wire script | `scripts/qa/ado-wire-pipeline.sh` | Manual org + push |

**Conclusion:** Documentation and CI YAML exist; **Test Plans work items and Bug # in dev.azure.com are not created yet** unless you ran bootstrap with a PAT.

---

## One-time setup (≈10 min)

### 1. Create org (browser)

1. https://dev.azure.com → **New organization** (e.g. `yangzhanggrainmartqa`)
2. **New project** → `GrainMart-QA` (private)

### 2. Create PAT

User settings → **Personal access tokens** → New token:

- Scopes: **Work Items (Read & write)**, **Test Management (Read & write)**
- Copy token

### 3. Run bootstrap

```bash
export AZURE_DEVOPS_EXT_PAT='your-pat-here'
python3 scripts/qa/ado-bootstrap.py --org yangzhanggrainmartqa --project GrainMart-QA
```

Creates:

- Test Plan **Purchase path** + suite **Checkout smoke**
- Test Cases **TC-01, TC-02, TC-03** (with steps)
- Bug **DEF-001** (Stripe webhook dedupe, Closed)

Manifest written to `docs/qa/ado-import/manifest.json` with URLs for resume/portfolio.

### 4. Screenshot evidence (optional)

Open Test Plans and Bug work item → screenshot → save under `docs/qa/evidence/ado-*.png`

### 5. Wire pipeline (optional)

```bash
bash scripts/qa/ado-wire-pipeline.sh yangzhanggrainmartqa GrainMart-QA
```

---

## Resume wording

**Before bootstrap (honest today):**

> Documented Azure DevOps–mapped test cases (TC-01…TC-03) and defect closed-loop (DEF-001/002); Azure Pipelines YAML mirrors JUnit + Playwright/Selenium gates.

**After bootstrap (use manifest URLs):**

> Maintained Azure DevOps Test Plans (TC-01…TC-03) and Bug work item (DEF-001) with repro/fix/re-verify; Azure Pipelines quality gates on merge.
