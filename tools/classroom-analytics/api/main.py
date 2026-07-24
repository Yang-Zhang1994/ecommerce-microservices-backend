"""FastAPI analytics service over PhysEd-style progress CSV / SQLite."""

from __future__ import annotations

import os
import sqlite3
from pathlib import Path

import pandas as pd
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = Path(os.environ.get("PROGRESS_CSV", ROOT / "data" / "sample_progress.csv"))
DB_PATH = Path(os.environ.get("ANALYTICS_DB", ROOT / "data" / "analytics.db"))
WEB_DIR = ROOT / "web"

app = FastAPI(title="Classroom Analytics Lab", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


def _load_frame() -> pd.DataFrame:
    if not CSV_PATH.exists():
        raise FileNotFoundError(f"CSV not found: {CSV_PATH}")
    df = pd.read_csv(CSV_PATH)
    df["completed"] = df["completed"].astype(str).str.lower().isin(["true", "1", "yes"])
    return df


def seed_sqlite() -> None:
    df = _load_frame()
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    with sqlite3.connect(DB_PATH) as conn:
        df.to_sql("progress", conn, if_exists="replace", index=False)


def query_df(sql: str) -> pd.DataFrame:
    if not DB_PATH.exists():
        seed_sqlite()
    with sqlite3.connect(DB_PATH) as conn:
        return pd.read_sql_query(sql, conn)


@app.on_event("startup")
def on_startup() -> None:
    seed_sqlite()


@app.get("/api/health")
def health() -> dict:
    return {"ok": True, "service": "classroom-analytics", "db": str(DB_PATH.name)}


def _normalize(df: pd.DataFrame) -> pd.DataFrame:
    out = df.copy()
    out["completed"] = (
        out["completed"].astype(str).str.lower().isin(["true", "1", "yes", "1.0"])
    )
    return out


@app.get("/api/summary")
def summary() -> dict:
    df = _normalize(query_df("SELECT * FROM progress"))
    if df.empty:
        raise HTTPException(status_code=404, detail="No progress rows")
    return {
        "students": int(df["student_id"].nunique()),
        "module_attempts": int(len(df)),
        "completion_rate_pct": round(100.0 * float(df["completed"].mean()), 1),
        "avg_quiz_score": round(float(df["quiz_score"].mean()), 1),
        "total_coins": int(df["coins_earned"].sum()),
    }


@app.get("/api/by-module")
def by_module() -> list[dict]:
    df = _normalize(query_df("SELECT * FROM progress"))
    grouped = (
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
    grouped = grouped.copy()
    grouped.loc[:, "avg_score"] = grouped["avg_score"].round(1)
    return grouped.to_dict(orient="records")


@app.get("/api/students")
def students() -> list[dict]:
    df = _normalize(query_df("SELECT * FROM progress"))
    grouped = (
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
    grouped = grouped.copy()
    grouped.loc[:, "avg_score"] = grouped["avg_score"].round(1)
    grouped.loc[:, "modules_completed"] = grouped["modules_completed"].astype(int)
    return grouped.to_dict(orient="records")


@app.get("/")
def index() -> FileResponse:
    index_path = WEB_DIR / "index.html"
    if not index_path.exists():
        raise HTTPException(status_code=404, detail="UI missing")
    return FileResponse(index_path)


if WEB_DIR.exists():
    app.mount("/static", StaticFiles(directory=str(WEB_DIR)), name="static")
