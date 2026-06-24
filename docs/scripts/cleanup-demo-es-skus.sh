#!/usr/bin/env bash
# Remove ES-only demo SKUs (no row in pms_sku_info). They appear in search but /item/{id} 404s.
# Usage: ./docs/scripts/cleanup-demo-es-skus.sh [ES_URL]
# Default ES_URL: http://localhost:9200

set -euo pipefail

ES_URL="${1:-http://localhost:9200}"
INDEX="product"
DEMO_IDS=(9001 9002 9003 9011 9012 9013)

echo "Deleting demo SKU docs from $ES_URL/$INDEX ..."
for id in "${DEMO_IDS[@]}"; do
  resp=$(curl -s -X DELETE "$ES_URL/$INDEX/_doc/$id")
  if echo "$resp" | grep -qE '"result":"deleted"|"result":"not_found"'; then
    echo "  $id removed"
  else
    echo "  $id FAIL: $resp"
  fi
done

count=$(curl -s "$ES_URL/$INDEX/_count" | grep -o '"count":[0-9]*' | cut -d: -f2)
echo "Done. Total docs in $INDEX: ${count:-?}"
