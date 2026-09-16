# GrainMart document-to-test planner

This tool connects the portfolio AI/RAG work to a real application repository.
It reads GrainMart QA plans, API/config contracts, and closed defect records,
then maps cited test cases to a small allowlist of executable test profiles.

The AI layer never creates or runs arbitrary shell commands:

1. BM25 retrieves relevant Markdown sections.
2. Optional OpenAI synthesis summarizes only retrieved context.
3. The planner extracts documented `TC-*` cases.
4. A human reviews the JSON plan.
5. Only a hard-coded Selenium or JUnit runner can execute.

## Plan from repository documentation

```bash
python3 tools/ai-test-planner/ai_test_planner.py \
  "guest checkout login regression" \
  --output build/ai-test-plan.json
```

Without an API key, the planner remains usable in deterministic offline BM25
mode. To add grounded LLM synthesis:

```bash
export OPENAI_API_KEY=...
python3 tools/ai-test-planner/ai_test_planner.py \
  "Stripe webhook replay duplicate payment"
```

## Execute an allowlisted profile

```bash
python3 tools/ai-test-planner/ai_test_planner.py \
  "guest checkout login regression" \
  --run selenium-checkout-smoke

python3 tools/ai-test-planner/ai_test_planner.py \
  "Stripe webhook replay duplicate payment" \
  --run stripe-webhook-unit
```

The first profile starts the local checkout fixture and runs the Java Selenium
Page Object smoke test. The second runs the documented Stripe webhook
idempotency JUnit test.

## Test the planner

```bash
python3 tools/ai-test-planner/test_ai_test_planner.py
```

## Resume-safe wording

> Integrated a document-grounded Python test planner into GrainMart: BM25 RAG
> reads QA plans, API contracts, and closed defects; maps cited cases to
> allowlisted Selenium/JUnit runners; and keeps human review before execution.

This is a portfolio workflow, not a production autonomous testing platform.
