# QA defect template

Copy into a GitHub Issue or Azure DevOps Work Item. Close only after **Re-verify** passes.

```text
Title: [area] short failure summary

## Repro steps
1.
2.
3.

## Expected
-

## Actual
-

## Severity
Critical | High | Medium | Low

## Environment
local kind | EKS | fixture | other:

## Fix PR
#

## Re-verify test
- [ ] Automated: (JUnit / Playwright / Selenium / Vitest) ________
- [ ] Manual: ________
- [ ] Result: PASS / FAIL
```

**Closed examples:** [DEF-001](defects/DEF-001-stripe-webhook-dedupe.md), [DEF-002](defects/DEF-002-admin-list-500.md).
