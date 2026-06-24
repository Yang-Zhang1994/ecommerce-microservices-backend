#!/usr/bin/env bash
# Normalize demo SPU rows for Mobile Phones (catalog 225): model names, descriptions, weights (kg).
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

update_spu() { post_json "${BASE}/product/spuinfo/update" "$1"; }

echo "=== Update Mobile Phone SPUs (catalog 225) ==="
update_spu '{"id":6,"spuName":"iPhone 15 Pro","spuDescription":"Apple A17 Pro chip, titanium design, ProMotion display.","catalogId":225,"brandId":5,"weight":0.19,"publishStatus":1,"createTime":"2026-02-17 15:53:08"}'
update_spu '{"id":7,"spuName":"Xiaomi 14","spuDescription":"Leica optics, Snapdragon 8 Gen 3, 4610mAh battery.","catalogId":225,"brandId":4,"weight":0.20,"publishStatus":1,"createTime":"2026-02-18 22:09:57"}'
update_spu '{"id":8,"spuName":"OPPO Reno15","spuDescription":"Portrait-focused camera system with sleek design.","catalogId":225,"brandId":1,"weight":0.19,"publishStatus":1,"createTime":"2026-03-14 02:54:36"}'

echo "Done. Refresh Product > SPU list."
