#!/usr/bin/env bash
# Deploy classroom-analytics to Azure App Service (Python) — no local Docker required.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RG="${1:-rg-classroom-analytics}"
APP="${2:-classroom-analytics-yz}"
LOC="${3:-canadacentral}"
PLAN="plan-classroom-analytics"

export PATH="/opt/homebrew/bin:${PATH}"

if ! az account show >/dev/null 2>&1; then
  echo "Run: az login --use-device-code"
  exit 1
fi

SUB_NAME=$(az account show --query name -o tsv)
echo "==> Subscription: $SUB_NAME"
echo "==> RG=$RG APP=$APP LOC=$LOC"

az group create -n "$RG" -l "$LOC" -o table >/dev/null
# B1 is available on free trial credits; F1 free tier is Linux-limited for some regions
az appservice plan create -g "$RG" -n "$PLAN" --is-linux --sku B1 -o table >/dev/null

if ! az webapp show -g "$RG" -n "$APP" >/dev/null 2>&1; then
  az webapp create -g "$RG" -p "$PLAN" -n "$APP" --runtime "PYTHON:3.12" -o table
fi

# App layout for Oryx: requirements at root + startup
STAGE=$(mktemp -d)
trap 'rm -rf "$STAGE"' EXIT
cp "$ROOT/requirements.txt" "$STAGE/"
cp -R "$ROOT/api" "$STAGE/api"
cp -R "$ROOT/web" "$STAGE/web"
cp -R "$ROOT/data" "$STAGE/data"
# Drop local DB so Azure reseeds from CSV
rm -f "$STAGE/data/analytics.db" "$STAGE/data/analytics.db-journal" 2>/dev/null || true
printf '%s\n' \
  'PROGRESS_CSV=data/sample_progress.csv' \
  'ANALYTICS_DB=/tmp/analytics.db' > "$STAGE/.env" 2>/dev/null || true

ZIP="$STAGE/app.zip"
(
  cd "$STAGE"
  zip -qr "$ZIP" requirements.txt api web data
)

echo "==> Zip deploy"
az webapp deploy -g "$RG" -n "$APP" --src-path "$ZIP" --type zip -o table

az webapp config set -g "$RG" -n "$APP" \
  --startup-file "uvicorn api.main:app --host 0.0.0.0 --port 8000" -o table >/dev/null

az webapp config appsettings set -g "$RG" -n "$APP" --settings \
  SCM_DO_BUILD_DURING_DEPLOYMENT=true \
  PROGRESS_CSV=data/sample_progress.csv \
  ANALYTICS_DB=/tmp/analytics.db \
  WEBSITES_PORT=8000 -o table >/dev/null

az webapp restart -g "$RG" -n "$APP" >/dev/null

URL="https://${APP}.azurewebsites.net"
echo "==> Deployed: $URL"
echo "    Health:  $URL/api/health"
echo "    Summary: $URL/api/summary"
echo "Waiting 40s for cold start…"
sleep 40
curl -sf "$URL/api/health" && echo || echo "(health not ready yet — open URL in browser in ~1 min)"
