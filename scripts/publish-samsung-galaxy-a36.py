#!/usr/bin/env python3
"""Publish Samsung Galaxy A36: upload desktop images, save SPU, 4 SKUs, shelf up."""
from __future__ import annotations

import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")
DESKTOP = Path(os.environ.get("DESKTOP", Path.home() / "Desktop"))

FILES = {
    "intro1": DESKTOP / "Samsung-Galaxy-A36-introduction-1.jpg",
    "intro2": DESKTOP / "Samsung-Galaxy-A36-introduction-2.jpg",
    "product1": DESKTOP / "Samsung-Galaxy-A36-product-1.png",
    "product2": DESKTOP / "Samsung-Galaxy-A36-product-2.png",
}

BRAND_ID = 8
CATALOG_ID = 225
WARE_ID = 1
STOCK_NUM = 200

# Sale attrs (order matches admin sale-attr list: RAM, Capacity, Color)
ATTR_RAM = 14
ATTR_CAPACITY = 8
ATTR_COLOR = 6

# Base spec attr ids
BASE_ATTRS = [
    {"attrId": 1, "attrValues": "2025", "showDesc": 0},
    {"attrId": 5, "attrValues": "5G;NFC;Wi-Fi 6E", "showDesc": 0},
    {"attrId": 7, "attrValues": "Glass", "showDesc": 1},
    {"attrId": 10, "attrValues": "6.7 inch", "showDesc": 1},
    {"attrId": 11, "attrValues": "LTPO AMOLED", "showDesc": 1},
    {"attrId": 13, "attrValues": "Exynos 1380", "showDesc": 1},
    {"attrId": 16, "attrValues": "5000mAh", "showDesc": 1},
]

SKU_MATRIX = [
    ("8GB", "128GB", "Black", "399.99"),
    ("8GB", "256GB", "Black", "449.99"),
    ("12GB", "128GB", "Black", "479.99"),
    ("12GB", "256GB", "Black", "529.99"),
]


def http_json(method: str, url: str, body=None, headers=None):
    data = None
    hdrs = {"Accept": "application/json"}
    if headers:
        hdrs.update(headers)
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        hdrs.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(url, data=data, headers=hdrs, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            raw = resp.read().decode("utf-8")
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        err = e.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {url} -> HTTP {e.code}: {err}") from e


def upload_file(path: Path) -> str:
    if not path.is_file():
        raise FileNotFoundError(path)
    presign = http_json("GET", f"{BASE}/third-party/s3/presigned-url?filename={path.name}")
    if presign.get("code") != 0:
        raise RuntimeError(f"presign failed: {presign}")
    data = presign["data"]
    upload_url = data["uploadUrl"]
    file_url = data["fileUrl"]

    content_type = "image/jpeg" if path.suffix.lower() in (".jpg", ".jpeg") else "image/png"
    proc = subprocess.run(
        [
            "curl", "-sS", "-f", "-X", "PUT",
            "-H", f"Content-Type: {content_type}",
            "--upload-file", str(path),
            upload_url,
        ],
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        raise RuntimeError(f"S3 PUT {path.name}: {proc.stderr or proc.stdout}")
    print(f"  uploaded {path.name} -> {file_url}")
    return file_url


def sale_attr_row(ram: str, cap: str, color: str):
    return [
        {"attrId": ATTR_RAM, "attrName": "RAM", "attrValue": ram},
        {"attrId": ATTR_CAPACITY, "attrName": "Capacity", "attrValue": cap},
        {"attrId": ATTR_COLOR, "attrName": "Color", "attrValue": color},
    ]


def build_skus(product_img: str, spu_name: str):
    spu_gallery = [
        {"imgUrl": product_img, "defaultImg": 1},
        {"imgUrl": product_img, "defaultImg": 0},
    ]
    skus = []
    for ram, cap, color, price in SKU_MATRIX:
        label = f"{ram} {cap} {color}"
        sku_images = [
            {"imgUrl": product_img, "defaultImg": 1},
            {"imgUrl": product_img, "defaultImg": 0},
        ]
        skus.append(
            {
                "attr": sale_attr_row(ram, cap, color),
                "skuName": f"{spu_name} {label}",
                "skuTitle": f"{spu_name} {label}",
                "skuSubtitle": "6.7-inch Super AMOLED 120Hz, 5000mAh, IP67",
                "price": price,
                "images": sku_images,
                "descar": [ram, cap, color],
                "fullCount": 0,
                "discount": 0,
                "countStatus": 0,
                "fullPrice": 0,
                "reducePrice": 0,
                "priceStatus": 0,
                "memberPrice": [],
            }
        )
    return skus, spu_gallery


def find_spu_id(name: str) -> int | None:
    import urllib.parse

    q = urllib.parse.urlencode({"page": 1, "limit": 10, "key": name})
    r = http_json("GET", f"{BASE}/product/spuinfo/list?{q}")
    for item in r.get("page", {}).get("list", []):
        if name.lower() in (item.get("spuName") or "").lower():
            return int(item["id"])
    return None


def list_sku_ids(spu_id: int) -> list[int]:
    import urllib.parse

    q = urllib.parse.urlencode({"page": 1, "limit": 50, "spuId": str(spu_id)})
    r = http_json("GET", f"{BASE}/product/skuinfo/list?{q}")
    ids = []
    for row in r.get("page", {}).get("list", []):
        if row.get("spuId") == spu_id or str(row.get("spuId")) == str(spu_id):
            ids.append(int(row["skuId"]))
    return ids


def add_ware_stock(sku_id: int):
    body = {"skuId": sku_id, "wareId": WARE_ID, "stock": STOCK_NUM}
    r = http_json("POST", f"{BASE}/ware/waresku/save", body)
    if r.get("code") != 0:
        print(f"  warn: ware stock sku {sku_id}: {r}", file=sys.stderr)


def main():
    print("=== Upload images from Desktop ===")
    urls = {k: upload_file(p) for k, p in FILES.items()}

    spu_name = "Samsung Galaxy A36 5G"
    existing = find_spu_id(spu_name)
    if existing:
        print(f"SPU already exists id={existing}; down first then re-run, or delete manually.")
        sys.exit(1)

    skus, gallery = build_skus(urls["product1"], spu_name)
    payload = {
        "spuName": spu_name,
        "spuDescription": (
            "Samsung Galaxy A36 5G — 6.7-inch FHD+ Super AMOLED 120Hz, Exynos 1380, "
            "5000mAh battery, IP67, dual SIM 5G. Awesome Intelligence with Circle to Search."
        ),
        "catalogId": CATALOG_ID,
        "brandId": BRAND_ID,
        "weight": "0.195",
        "publishStatus": 0,
        "decript": [urls["intro1"], urls["intro2"]],
        "images": [
            {"imgUrl": urls["product1"], "defaultImg": 1},
            {"imgUrl": urls["product2"], "defaultImg": 0},
        ],
        "bounds": {"buyBounds": 1000, "growBounds": 1000},
        "baseAttrs": BASE_ATTRS,
        "skus": skus,
    }

    print("\n=== Save SPU + 4 SKUs ===")
    save_r = http_json("POST", f"{BASE}/product/spuinfo/save", payload)
    if save_r.get("code") != 0:
        raise RuntimeError(f"save failed: {save_r}")
    print("  save ok")

    spu_id = find_spu_id(spu_name)
    if not spu_id:
        raise RuntimeError("SPU not found after save")
    print(f"  spuId={spu_id}")

    sku_ids = list_sku_ids(spu_id)
    print(f"  skuIds={sku_ids}")

    print("\n=== Ware stock ===")
    for sid in sku_ids:
        add_ware_stock(sid)

    print("\n=== Shelf up (Elasticsearch) ===")
    up_r = http_json("POST", f"{BASE}/product/spuinfo/{spu_id}/up")
    print(f"  up: {up_r}")

    print("\n=== Done ===")
    print(f"Mall item example: /item/{sku_ids[0] if sku_ids else '?'}")
    print(f"Search: catalog3Id={CATALOG_ID}, brandId={BRAND_ID}")


if __name__ == "__main__":
    main()
