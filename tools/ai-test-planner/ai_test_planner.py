#!/usr/bin/env python3
"""Document-grounded test planner with allowlisted local execution.

The retrieval/LLM layer may recommend test profiles, but it never creates or
executes shell commands. Only profiles defined in RUNNERS can run.
"""

from __future__ import annotations

import argparse
import json
import math
import os
import re
import subprocess
import time
import urllib.request
from collections import Counter
from dataclasses import asdict, dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DOC_PATHS = (
    Path("docs/qa-test-plan.md"),
    Path("docs/api-contracts-config-to-downstream.md"),
    Path("docs/qa/defects/DEF-001-stripe-webhook-dedupe.md"),
    Path("docs/qa/defects/DEF-002-admin-list-500.md"),
)
TOKEN_RE = re.compile(r"[a-zA-Z][a-zA-Z0-9_-]*|TC-\d+", re.IGNORECASE)
CASE_RE = re.compile(r"\|\s*(TC-\d+)\s*\|([^|]+)\|([^|]+)\|([^|]+)\|")


@dataclass(frozen=True)
class Chunk:
    source: str
    heading: str
    text: str


@dataclass(frozen=True)
class TestCase:
    case_id: str
    steps: str
    expected: str
    automation: str


PROFILE_BY_CASE = {
    "TC-01": "selenium-checkout-smoke",
    "TC-02": "selenium-checkout-smoke",
    "TC-05": "stripe-webhook-unit",
}


def tokenize(text: str) -> list[str]:
    return [token.lower() for token in TOKEN_RE.findall(text)]


def load_chunks() -> list[Chunk]:
    chunks: list[Chunk] = []
    for relative_path in DOC_PATHS:
        text = (ROOT / relative_path).read_text(encoding="utf-8")
        sections = re.split(r"(?m)(?=^##?\s+)", text)
        for section in sections:
            section = section.strip()
            if not section:
                continue
            first_line = section.splitlines()[0]
            heading = re.sub(r"^#+\s*", "", first_line)
            chunks.append(Chunk(str(relative_path), heading, section))
    return chunks


def retrieve(query: str, limit: int = 4) -> list[tuple[Chunk, float]]:
    """Return BM25-ranked Markdown sections."""
    chunks = load_chunks()
    documents = [tokenize(chunk.text) for chunk in chunks]
    query_tokens = tokenize(query)
    average_length = sum(map(len, documents)) / max(len(documents), 1)
    document_frequency = Counter()
    for document in documents:
        document_frequency.update(set(document))

    ranked: list[tuple[Chunk, float]] = []
    for chunk, document in zip(chunks, documents):
        frequencies = Counter(document)
        score = 0.0
        for token in query_tokens:
            frequency = frequencies[token]
            if not frequency:
                continue
            inverse_frequency = math.log(
                1 + (len(documents) - document_frequency[token] + 0.5)
                / (document_frequency[token] + 0.5)
            )
            denominator = frequency + 1.5 * (
                1 - 0.75 + 0.75 * len(document) / max(average_length, 1)
            )
            score += inverse_frequency * frequency * 2.5 / denominator
        if score:
            ranked.append((chunk, round(score, 4)))
    return sorted(ranked, key=lambda item: item[1], reverse=True)[:limit]


def extract_test_cases(hits: list[tuple[Chunk, float]]) -> list[TestCase]:
    cases: dict[str, TestCase] = {}
    for chunk, _ in hits:
        for match in CASE_RE.finditer(chunk.text):
            case = TestCase(
                case_id=match.group(1).strip(),
                steps=match.group(2).strip(),
                expected=match.group(3).strip(),
                automation=match.group(4).strip(),
            )
            cases[case.case_id] = case
    return list(cases.values())


def llm_summary(query: str, hits: list[tuple[Chunk, float]], cases: list[TestCase]) -> tuple[str, str]:
    """Optionally synthesize a grounded summary; offline output remains usable."""
    api_key = os.getenv("OPENAI_API_KEY")
    citations = "\n\n".join(
        f"[{chunk.source} — {chunk.heading}]\n{chunk.text[:1200]}"
        for chunk, _ in hits
    )
    if not api_key:
        ids = ", ".join(case.case_id for case in cases) or "no mapped test cases"
        return "offline_bm25", f"Retrieved {len(hits)} cited sections; mapped {ids}."

    payload = {
        "model": os.getenv("OPENAI_MODEL", "gpt-4o-mini"),
        "temperature": 0,
        "messages": [
            {
                "role": "system",
                "content": (
                    "You are a QA planning assistant. Use only supplied repository "
                    "documentation. Cite source paths, do not invent endpoints or "
                    "test results, and recommend only listed case IDs."
                ),
            },
            {
                "role": "user",
                "content": f"Request: {query}\n\nRepository context:\n{citations}",
            },
        ],
    }
    request = urllib.request.Request(
        "https://api.openai.com/v1/chat/completions",
        data=json.dumps(payload).encode(),
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        },
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        body = json.load(response)
    return "openai_grounded", body["choices"][0]["message"]["content"]


def build_plan(query: str) -> dict:
    hits = retrieve(query)
    cases = extract_test_cases(hits)
    mode, summary = llm_summary(query, hits, cases)
    profiles = sorted(
        {
            PROFILE_BY_CASE[case.case_id]
            for case in cases
            if case.case_id in PROFILE_BY_CASE
        }
    )
    return {
        "query": query,
        "mode": mode,
        "summary": summary,
        "citations": [
            {
                "source": chunk.source,
                "heading": chunk.heading,
                "score": score,
            }
            for chunk, score in hits
        ],
        "documented_test_cases": [asdict(case) for case in cases],
        "allowlisted_profiles": profiles,
        "review_required": True,
    }


def run_selenium_checkout_smoke() -> int:
    fixture_dir = ROOT / "gulimall-mall/e2e/fixtures/checkout-path"
    server = subprocess.Popen(
        [
            "python3",
            "-m",
            "http.server",
            "4173",
            "--bind",
            "127.0.0.1",
            "--directory",
            str(fixture_dir),
        ],
        cwd=ROOT,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    try:
        for _ in range(40):
            try:
                urllib.request.urlopen("http://127.0.0.1:4173/index.html", timeout=1)
                break
            except OSError:
                time.sleep(0.25)
        else:
            raise RuntimeError("checkout fixture did not start")
        environment = os.environ.copy()
        environment["E2E_BASE_URL"] = "http://127.0.0.1:4173"
        result = subprocess.run(
            ["mvn", "-f", "tools/selenium-smoke/pom.xml", "test"],
            cwd=ROOT,
            env=environment,
            check=False,
        )
        return result.returncode
    finally:
        server.terminate()
        server.wait(timeout=5)


def run_stripe_webhook_unit() -> int:
    result = subprocess.run(
        [
            "mvn",
            "-pl",
            "gulimall-order",
            "-am",
            "test",
            "-Dtest=StripePaymentServiceImplTest",
            "-Dsurefire.failIfNoSpecifiedTests=false",
        ],
        cwd=ROOT,
        check=False,
    )
    return result.returncode


RUNNERS = {
    "selenium-checkout-smoke": run_selenium_checkout_smoke,
    "stripe-webhook-unit": run_stripe_webhook_unit,
}


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("query", nargs="?", default="checkout and payment regression")
    parser.add_argument("--run", choices=sorted(RUNNERS))
    parser.add_argument("--output", type=Path)
    arguments = parser.parse_args()

    plan = build_plan(arguments.query)
    rendered = json.dumps(plan, indent=2, ensure_ascii=False)
    print(rendered)
    if arguments.output:
        arguments.output.parent.mkdir(parents=True, exist_ok=True)
        arguments.output.write_text(rendered + "\n", encoding="utf-8")
    if arguments.run:
        raise SystemExit(RUNNERS[arguments.run]())


if __name__ == "__main__":
    main()
