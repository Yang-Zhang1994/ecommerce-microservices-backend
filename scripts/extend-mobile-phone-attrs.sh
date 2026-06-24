#!/usr/bin/env bash
# Mobile Phones (225): Battery as base spec; RAM/Capacity as sale attrs (see setup-mobile-phone-sale-attrs.sh).
set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"

post_json() {
  local url="$1"
  local payload="$2"
  curl -sS -X POST "$url" -H 'Content-Type: application/json' -d "$payload" | python3 -c "
import sys, json
d = json.load(sys.stdin)
if d.get('code') != 0:
    print(d)
    sys.exit(1)
print('ok:', d.get('msg', 'success'))
"
}

save_attr() { post_json "${BASE}/product/attr/save" "$1"; }

echo "=== Base attribute: Battery only (spec step 2) ==="
save_attr '{"attrName":"Battery","valueType":0,"valueSelect":"4000mAh;4500mAh;5000mAh;5500mAh;6000mAh","searchType":0,"showDesc":1,"icon":"6","attrType":1,"enable":1,"catelogId":225,"attrGroupId":2}'

echo "=== Sale attrs: run ./scripts/setup-mobile-phone-sale-attrs.sh ==="
"$(dirname "$0")/setup-mobile-phone-sale-attrs.sh"
