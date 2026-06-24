#!/usr/bin/env bash
# Normalize Mobile Phones (225) sale attributes after review.
set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"

post_json() {
  curl -sS -X POST "$1" -H 'Content-Type: application/json' -d "$2" | python3 -c "
import sys, json
d = json.load(sys.stdin)
if d.get('code') != 0:
    print(d); sys.exit(1)
print('ok')
"
}

update_attr() { post_json "${BASE}/product/attr/update" "$1"; }

echo "=== Sale attributes: Color + Capacity (SKU), disable Edition ==="
update_attr '{"attrId":6,"attrName":"Color","valueType":1,"valueSelect":"Black;White;Blue;Silver;Gold;Green;Pink","searchType":0,"showDesc":0,"icon":"","attrType":0,"enable":1,"catelogId":225}'
update_attr '{"attrId":8,"attrName":"Capacity","valueType":1,"valueSelect":"128GB;256GB;512GB;1TB","searchType":0,"showDesc":0,"icon":"","attrType":0,"enable":1,"catelogId":225}'
update_attr '{"attrId":9,"attrName":"Edition","valueType":1,"valueSelect":"Standard;Pro;Ultra","searchType":0,"showDesc":0,"icon":"","attrType":0,"enable":0,"catelogId":225}'

echo "Done. Sale Attributes tab: use Color + Capacity when publishing phones."
