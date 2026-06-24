#!/usr/bin/env bash
# Refresh wms_ware_sku.sku_name from product catalog; default stock_locked to 0.
set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"
echo "POST ${BASE}/ware/waresku/syncFromProduct"
curl -sS -X POST "${BASE}/ware/waresku/syncFromProduct" -H 'Content-Type: application/json' | python3 -m json.tool
