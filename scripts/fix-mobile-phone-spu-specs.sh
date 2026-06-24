#!/usr/bin/env bash
# Set realistic base specifications for Mobile Phone SPUs (catalog 225).
# Values must match pms_attr.value_select options where applicable.
set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"

post_specs() {
  local spu_id="$1"
  local payload="$2"
  curl -sS -X POST "${BASE}/product/attr/update/${spu_id}" \
    -H 'Content-Type: application/json' \
    -d "$payload" | python3 -c "
import sys, json
d = json.load(sys.stdin)
if d.get('code') != 0:
    print('SPU', '${spu_id}', 'failed:', d)
    sys.exit(1)
print('SPU', '${spu_id}', 'ok')
"
}

echo "=== iPhone 15 Pro (SPU 6) ==="
post_specs 6 '[
  {"attrId":10,"attrName":"Screen Size","attrValue":"6.1 inch","quickShow":1},
  {"attrId":11,"attrName":"Screen Material","attrValue":"LTPO AMOLED","quickShow":1},
  {"attrId":1,"attrName":"Release Year","attrValue":"2023","quickShow":1},
  {"attrId":5,"attrName":"Network","attrValue":"5G;Wi-Fi 6E;NFC","quickShow":1},
  {"attrId":7,"attrName":"Body Material","attrValue":"Stainless Steel","quickShow":1},
  {"attrId":12,"attrName":"Process Node","attrValue":"3nm","quickShow":1},
  {"attrId":13,"attrName":"Processor","attrValue":"Apple A17 Pro","quickShow":1},
  {"attrId":14,"attrName":"RAM","attrValue":"8GB","quickShow":1},
  {"attrId":15,"attrName":"Storage","attrValue":"128GB;256GB;512GB;1TB","quickShow":1},
  {"attrId":16,"attrName":"Battery","attrValue":"4000mAh","quickShow":1}
]'

echo "=== Xiaomi 14 (SPU 7) ==="
post_specs 7 '[
  {"attrId":10,"attrName":"Screen Size","attrValue":"6.3 inch","quickShow":1},
  {"attrId":11,"attrName":"Screen Material","attrValue":"AMOLED","quickShow":1},
  {"attrId":1,"attrName":"Release Year","attrValue":"2024","quickShow":1},
  {"attrId":5,"attrName":"Network","attrValue":"5G;Wi-Fi 6;NFC","quickShow":1},
  {"attrId":7,"attrName":"Body Material","attrValue":"Glass","quickShow":1},
  {"attrId":12,"attrName":"Process Node","attrValue":"4nm","quickShow":1},
  {"attrId":13,"attrName":"Processor","attrValue":"Snapdragon 8 Gen 3","quickShow":1},
  {"attrId":14,"attrName":"RAM","attrValue":"8GB;12GB;16GB","quickShow":1},
  {"attrId":15,"attrName":"Storage","attrValue":"256GB;512GB","quickShow":1},
  {"attrId":16,"attrName":"Battery","attrValue":"4500mAh","quickShow":1}
]'

echo "=== OPPO Reno15 (SPU 8) ==="
post_specs 8 '[
  {"attrId":10,"attrName":"Screen Size","attrValue":"6.7 inch","quickShow":1},
  {"attrId":11,"attrName":"Screen Material","attrValue":"AMOLED","quickShow":1},
  {"attrId":1,"attrName":"Release Year","attrValue":"2025","quickShow":1},
  {"attrId":5,"attrName":"Network","attrValue":"5G;4G LTE;NFC","quickShow":1},
  {"attrId":7,"attrName":"Body Material","attrValue":"Glass","quickShow":1},
  {"attrId":12,"attrName":"Process Node","attrValue":"4nm","quickShow":1},
  {"attrId":13,"attrName":"Processor","attrValue":"Dimensity 9300","quickShow":1},
  {"attrId":14,"attrName":"RAM","attrValue":"12GB;16GB","quickShow":1},
  {"attrId":15,"attrName":"Storage","attrValue":"256GB;512GB","quickShow":1},
  {"attrId":16,"attrName":"Battery","attrValue":"5000mAh","quickShow":1}
]'

echo "Done. Open Product > SPU > Specifications to verify."
