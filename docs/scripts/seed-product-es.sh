#!/usr/bin/env bash
# Seed test product data into Elasticsearch (product index).
# WARNING: SKUs 9001-9013 are ES-only (not in MySQL). Search will list them but /item/{skuId} 404s.
# Remove with: ./docs/scripts/cleanup-demo-es-skus.sh [ES_URL]
# Index shape matches production: brandId/catalogId + attrs(attrId, attrValue) only (no display fields).
# Usage: ./docs/scripts/seed-product-es.sh [ES_URL]
# Default ES_URL: http://localhost:9200

ES_URL="${1:-http://localhost:9200}"
INDEX="product"

IMG_APPLE="https://ecommerce-uploads-2.s3.us-east-2.amazonaws.com/2026-02-17/0daaa519-5d2e-4e46-9e11-2b1b6fe43bc6_2b1837c6c50add30.jpg"
IMG_HUAWEI="https://ecommerce-uploads-2.s3.us-east-2.amazonaws.com/2026-02-17/ea7e27f5-e2a1-4b84-8440-dd65f213ab8f_3c24f9cd69534030.jpg"
IMG_XIAOMI="https://ecommerce-uploads-oregon.s3.us-west-2.amazonaws.com/2026-05-19/4c1772e6-acf1-4a58-8d90-52bd82f27c0b_shopping.png"
IMG_SAMSUNG="https://ecommerce-uploads-oregon.s3.us-west-2.amazonaws.com/2026-05-19/f39469ef-a1de-4f87-b843-15de238648d3_shopping__1_.png"
IMG_OPPO="https://ecommerce-uploads-oregon.s3.us-west-2.amazonaws.com/2026-05-19/0828b298-ec75-4bae-932a-c50cbd7da17f_shopping__2_.png"
IMG_VIVO="https://ecommerce-uploads-2.s3.us-east-2.amazonaws.com/2026-02-17/471f0bf9-7ef6-4865-9ee1-3c9d4df110be_23cd65077f12f7f5.jpg"

echo "Writing test data to $ES_URL/$INDEX ..."

for id in 9001 9002 9003 9011 9012 9013; do
  case $id in
    9001) title="Apple iPhone 15 Pro Max 256GB Blue"; brand=5; price=9999; img="$IMG_APPLE";;
    9002) title="Huawei Mate 60 Pro 512GB Black"; brand=7; price=6999; img="$IMG_HUAWEI";;
    9003) title="Xiaomi 14 Ultra 16GB 512GB White"; brand=4; price=5999; img="$IMG_XIAOMI";;
    9011) title="Samsung Galaxy S24 Ultra 256GB"; brand=8; price=8999; img="$IMG_SAMSUNG";;
    9012) title="OPPO Find X7 512GB"; brand=1; price=5499; img="$IMG_OPPO";;
    9013) title="Vivo X100 Pro 16GB 512GB"; brand=2; price=5999; img="$IMG_VIVO";;
  esac
  resp=$(curl -s -X POST "$ES_URL/$INDEX/_doc/$id" -H "Content-Type: application/json" -d "{
    \"skuId\": $id,
    \"spuId\": $((id+900)),
    \"skuTitle\": \"$title\",
    \"skuPrice\": $price,
    \"skuImg\": \"$img\",
    \"saleCount\": 500,
    \"hasStock\": true,
    \"hotScore\": 8000,
    \"brandId\": $brand,
    \"catalogId\": 225,
    \"attrs\": [
      {\"attrId\": 10, \"attrValue\": \"6.7 inch\"},
      {\"attrId\": 15, \"attrValue\": \"512GB\"}
    ]
  }")
  if echo "$resp" | grep -q '"result"'; then
    echo "  $id OK"
  else
    echo "  $id FAIL: $resp"
  fi
done

count=$(curl -s "$ES_URL/$INDEX/_count" | grep -o '"count":[0-9]*' | cut -d: -f2)
echo "Done. Total docs in $INDEX: $count"
