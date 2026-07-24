#!/usr/bin/env python3
"""Summarize PhysEd-style progress CSV with pandas (classroom analytics demo).

Usage:
  python3 scripts/report_progress.py
  python3 scripts/report_progress.py --csv data/sample_progress.csv --out out/
"""

from __future__ import annotations

import argparse
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CSV = ROOT / "data" / "sample_progress.csv"
DEFAULT_OUT = ROOT / "out"


def build_reports(csv_path: Path, out_dir: Path) -> dict:
    df = pd.read_csv(csv_path).copy()
    df.loc[:, "completed"] = (
        df["completed"].astype(str).str.lower().isin(["true", "1", "yes"])
    )

    summary = {
        "students": int(df["student_id"].nunique()),
        "module_attempts": int(len(df)),
        "completion_rate_pct": round(100.0 * float(df["completed"].mean()), 1),
        "avg_quiz_score": round(float(df["quiz_score"].mean()), 1),
        "total_coins": int(df["coins_earned"].sum()),
    }

    by_module = (
        df.groupby(["module_id", "module_name"], as_index=False)
        .agg(
            attempts=("student_id", "count"),
            students=("student_id", "nunique"),
            completion_rate_pct=("completed", lambda s: round(100.0 * float(s.mean()), 1)),
            avg_score=("quiz_score", "mean"),
            coins=("coins_earned", "sum"),
        )
        .sort_values("module_id")
    )
    by_module = by_module.copy()
    by_module.loc[:, "avg_score"] = by_module["avg_score"].round(1)

    by_student = (
        df.groupby(["student_id", "student_name"], as_index=False)
        .agg(
            modules_touched=("module_id", "nunique"),
            modules_completed=("completed", "sum"),
            avg_score=("quiz_score", "mean"),
            coins=("coins_earned", "sum"),
            last_activity=("last_activity", "max"),
        )
        .sort_values("coins", ascending=False)
    )
    by_student = by_student.copy()
    by_student.loc[:, "avg_score"] = by_student["avg_score"].round(1)
    by_student.loc[:, "modules_completed"] = by_student["modules_completed"].astype(int)

    out_dir.mkdir(parents=True, exist_ok=True)
    summary_path = out_dir / "summary.json"
    module_path = out_dir / "by_module.csv"
    student_path = out_dir / "by_student.csv"

    pd.Series(summary).to_json(summary_path, indent=2)
    by_module.to_csv(module_path, index=False)
    by_student.to_csv(student_path, index=False)

    # Optional chart (skip cleanly if matplotlib missing)
    chart_path = out_dir / "completion_by_module.png"
    try:
        import matplotlib.pyplot as plt

        fig, ax = plt.subplots(figsize=(7, 4))
        ax.bar(by_module["module_name"], by_module["completion_rate_pct"], color="#2563eb")
        ax.set_ylabel("Completion %")
        ax.set_title("Module completion rate")
        ax.set_ylim(0, 100)
        plt.xticks(rotation=20, ha="right")
        fig.tight_layout()
        fig.savefig(chart_path, dpi=120)
        plt.close(fig)
    except Exception as exc:  # noqa: BLE001
        chart_path = None
        print(f"(chart skipped: {exc})")

    return {
        "summary": summary,
        "summary_path": str(summary_path),
        "module_path": str(module_path),
        "student_path": str(student_path),
        "chart_path": str(chart_path) if chart_path else None,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="PhysEd progress pandas report")
    parser.add_argument("--csv", type=Path, default=DEFAULT_CSV)
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    args = parser.parse_args()

    result = build_reports(args.csv, args.out)
    print("summary:", result["summary"])
    print("wrote:", result["summary_path"])
    print("wrote:", result["module_path"])
    print("wrote:", result["student_path"])
    if result["chart_path"]:
        print("wrote:", result["chart_path"])


if __name__ == "__main__":
    main()
