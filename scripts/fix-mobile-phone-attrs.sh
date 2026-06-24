#!/usr/bin/env bash
# Apply mobile phone (catelog 225) attribute fixes via product API.
set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"

post_attr() {
  local payload="$1"
  curl -sS -X POST "${BASE}/product/attr/update" \
    -H 'Content-Type: application/json' \
    -d "$payload" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('msg', d)); sys.exit(0 if d.get('code')==0 else 1)"
}

# attrId, name, valueType, valueSelect, searchType, showDesc, icon, attrGroupId
while IFS='|' read -r id name vtype vsel search show icon grp; do
  [[ "$id" == \#* ]] && continue
  [[ -z "$id" ]] && continue
  payload=$(cat <<EOF
{"attrId":$id,"attrName":"$name","valueType":$vtype,"valueSelect":"$vsel","searchType":$search,"showDesc":$show,"icon":"$icon","attrType":1,"enable":1,"catelogId":225,"attrGroupId":$grp}
EOF
)
  echo "Updating attr $id ($name)..."
  post_attr "$payload"
done <<'DATA'
10|Screen Size|0|6.1 inch;6.3 inch;6.5 inch;6.7 inch;6.8 inch|1|1|0|1
11|Screen Material|0|OLED;AMOLED;LTPO AMOLED;IPS LCD|1|1|1|1
12|Process Node|0|3nm;4nm;5nm;6nm|0|0|1|3
13|Processor|0|Snapdragon 8 Gen 3;Snapdragon 8 Gen 2;Apple A18 Pro;Apple A17 Pro;Dimensity 9300;Exynos 2400|1|1|2|3
1|Release Year|0|2022;2023;2024;2025;2026|1|0|0|2
5|Network|1|5G;4G LTE;Wi-Fi 6;Wi-Fi 6E;NFC|1|0|1|2
7|Body Material|0|Glass;Aluminum Alloy;Stainless Steel;Ceramic;Plastic|1|1|3|2
DATA

echo "Done. Refresh Base Attributes for Mobile Phones in admin."
