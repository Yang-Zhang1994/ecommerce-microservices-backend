#!/usr/bin/env bash
# Configure Mobile Phones (225): RAM + Capacity as sale attrs (Publish SPU step 3 checkboxes).
# Disables duplicate base Storage; converts RAM from base to sale.
#
# Usage:
#   ./scripts/setup-mobile-phone-sale-attrs.sh
#   GULIMALL_API=http://127.0.0.1:88/api ./scripts/setup-mobile-phone-sale-attrs.sh
#
# Requires: gateway + gulimall-product up. For DB, also run:
#   mysql ... < gulimall-product/src/main/resources/db/mobile-phone-sale-attrs-ram.sql

set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

post_json() {
  local url="$1"
  local payload="$2"
  curl -sS -X POST "$url" -H 'Content-Type: application/json' -d "$payload" | python3 -c "
import sys, json
d = json.load(sys.stdin)
if d.get('code') != 0:
    print('FAIL', d)
    sys.exit(1)
print('ok:', d.get('msg', 'success'))
"
}

update_attr() { post_json "${BASE}/product/attr/update" "$1"; }

echo "=== Sale attributes (Publish SPU step 3) ==="
update_attr '{"attrId":14,"attrName":"RAM","valueType":1,"valueSelect":"4GB;8GB;12GB;16GB;24GB","searchType":1,"showDesc":0,"icon":"","attrType":0,"enable":1,"catelogId":225}'
update_attr '{"attrId":8,"attrName":"Capacity","valueType":1,"valueSelect":"128GB;256GB;512GB;1TB","searchType":1,"showDesc":0,"icon":"","attrType":0,"enable":1,"catelogId":225}'
update_attr '{"attrId":6,"attrName":"Color","valueType":1,"valueSelect":"Black;White;Blue;Silver;Gold;Green;Pink","searchType":0,"showDesc":0,"icon":"","attrType":0,"enable":1,"catelogId":225}'
update_attr '{"attrId":9,"attrName":"Edition","valueType":1,"valueSelect":"Standard;Pro;Ultra","searchType":0,"showDesc":0,"icon":"","attrType":0,"enable":0,"catelogId":225}'

echo "=== Disable base Storage (use Capacity in step 3) ==="
update_attr '{"attrId":15,"attrName":"Storage","valueType":0,"valueSelect":"128GB;256GB;512GB;1TB","searchType":0,"showDesc":0,"icon":"5","attrType":1,"enable":0,"catelogId":225,"attrGroupId":2}'

echo ""
echo "Verify sale attrs:"
curl -sS "${BASE}/product/attr/sale/list/225?page=1&limit=20" | python3 -c "
import sys, json
d = json.load(sys.stdin)
for r in d.get('page', {}).get('list', []):
    if r.get('enable') == 1:
        print(' ', r.get('attrId'), r.get('attrName', '').strip(), 'valueType', r.get('valueType'))
"

echo ""
echo "Done. In Publish SPU: step 2 = specs only; step 3 = Color + RAM + Capacity (multi-select)."
echo "Legacy SKU rows (Version/Memory): run scripts/migrate-mobile-phone-sale-attrs-unified.py"
echo "If RAM still appears in step 2, run SQL: ${ROOT}/gulimall-product/src/main/resources/db/mobile-phone-sale-attrs-ram.sql"
