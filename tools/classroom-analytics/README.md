# Classroom analytics lab (data tooling demo)

Python **pandas** reporting + **FastAPI** analytics APIs + a small dashboard UI.
Sample CSV mirrors BC PhysEd–style classroom progress (modules, scores, coins).

## Local

```bash
cd tools/classroom-analytics
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# 1) Pandas report → out/summary.json, by_module.csv, chart
python3 scripts/report_progress.py

# 2) API + UI
uvicorn api.main:app --reload --port 8000
# open http://127.0.0.1:8000
```

### API

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/health` | Liveness |
| GET | `/api/summary` | Class-level KPIs |
| GET | `/api/by-module` | Completion by module |
| GET | `/api/students` | Per-student rollup |

## Azure

Needs a **subscription** (personal Azure free tier works). NEU tenant login alone is not enough.

```bash
az login
az account list -o table
az account set --subscription "<your-sub>"
bash tools/classroom-analytics/scripts/azure-deploy.sh
```

Pipeline smoke (pandas + API): `tools/classroom-analytics/azure-pipelines-analytics.yml`  
Repo also has GrainMart QA gates in root `azure-pipelines.yml`.
