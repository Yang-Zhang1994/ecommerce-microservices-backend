#!/usr/bin/env bash
# Deploy tools/classroom-analytics to Azure App Service (Linux container).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RG="${1:-rg-classroom-analytics}"
APP="${2:-classroom-analytics-$USER}"
LOC="${3:-canadacentral}"
ACR="acra${APP//-/}"
ACR="$(echo "$ACR" | tr '[:upper:]' '[:lower:]' | cut -c1-40)"

export PATH="/opt/homebrew/bin:${PATH}"

if ! az account show >/dev/null 2>&1; then
  echo "Run: az login   (prefer a personal Microsoft account with a free subscription)"
  exit 1
fi

SUB=$(az account show --query id -o tsv 2>/dev/null || true)
NAME=$(az account show --query name -o tsv 2>/dev/null || true)
if [[ -z "$SUB" || "$NAME" == "N/A(tenant level account)" ]]; then
  echo "No usable Azure subscription on this account."
  echo "1) Create free account: https://azure.microsoft.com/free/"
  echo "2) az login && az account set --subscription <id>"
  echo "3) Re-run this script."
  exit 2
fi

echo "==> Subscription: $NAME ($SUB)"
echo "==> RG=$RG APP=$APP LOC=$LOC ACR=$ACR"

az group create -n "$RG" -l "$LOC" -o table
az acr create -g "$RG" -n "$ACR" --sku Basic --admin-enabled true -o table
az acr login -n "$ACR"

IMAGE="$ACR.azurecr.io/classroom-analytics:latest"
docker build -t "$IMAGE" "$ROOT"
docker push "$IMAGE"

ACR_USER=$(az acr credential show -n "$ACR" --query username -o tsv)
ACR_PASS=$(az acr credential show -n "$ACR" --query passwords[0].value -o tsv)

az appservice plan create -g "$RG" -n "plan-$APP" --is-linux --sku B1 -o table
az webapp create -g "$RG" -p "plan-$APP" -n "$APP" --deployment-container-image-name "$IMAGE" -o table
az webapp config container set -g "$RG" -n "$APP" \
  --docker-custom-image-name "$IMAGE" \
  --docker-registry-server-url "https://$ACR.azurecr.io" \
  --docker-registry-server-user "$ACR_USER" \
  --docker-registry-server-password "$ACR_PASS" -o table
az webapp config appsettings set -g "$RG" -n "$APP" --settings \
  WEBSITES_PORT=8000 \
  PROGRESS_CSV=/app/data/sample_progress.csv \
  ANALYTICS_DB=/app/data/analytics.db -o table

URL="https://${APP}.azurewebsites.net"
echo "==> Deployed: $URL"
echo "    Health:   $URL/api/health"
echo "    Summary:  $URL/api/summary"
