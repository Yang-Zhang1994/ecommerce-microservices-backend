#!/usr/bin/env bash
# Revert scripts/upload-spu-gallery-images.sh — restore pre-script state for SPUs 6/7/8.
set -euo pipefail
BASE="${GULIMALL_API:-http://127.0.0.1:88/api}"

save_spu_gallery() {
  local spu_id="$1"
  local json="$2"
  curl -sS -X POST "${BASE}/product/spuimages/saveBatch" \
    -H 'Content-Type: application/json' \
    -d "{\"spuId\":${spu_id},\"images\":${json}}" | python3 -c "
import sys, json
d = json.load(sys.stdin)
if d.get('code') != 0:
    print('SPU', '${spu_id}', 'failed:', d)
    sys.exit(1)
print('SPU', '${spu_id}', 'reverted')
"
}

echo "=== Revert SPU gallery upload (6/7/8) ==="

# SPU 6: had no gallery before the bulk upload script
save_spu_gallery 6 '[]'

# SPU 7: restore the three images already in admin before the script (original S3 keys)
save_spu_gallery 7 '[
  {"imgUrl":"https://ecommerce-uploads-oregon.s3.us-west-2.amazonaws.com/2026-05-19/4c1772e6-acf1-4a58-8d90-52bd82f27c0b_shopping.png","imgSort":0,"defaultImg":1},
  {"imgUrl":"https://ecommerce-uploads-oregon.s3.us-west-2.amazonaws.com/2026-05-19/f39469ef-a1de-4f87-b843-15de238648d3_shopping__1_.png","imgSort":1,"defaultImg":0},
  {"imgUrl":"https://ecommerce-uploads-oregon.s3.us-west-2.amazonaws.com/2026-05-19/0828b298-ec75-4bae-932a-c50cbd7da17f_shopping__2_.png","imgSort":2,"defaultImg":0}
]'

# SPU 8: clear images added by the script (none in DB before ids 65–67)
save_spu_gallery 8 '[]'

echo "Done."
