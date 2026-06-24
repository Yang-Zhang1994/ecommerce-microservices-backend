#!/usr/bin/env python3
"""
OPPO Reno15 (SPU 8): Black -> Brown; reasonable USD prices.
Xiaomi 14 (SPU 7): reasonable USD prices.

Usage:
  GULIMALL_API=http://127.0.0.1:88/api python3 scripts/sync-oppo-xiaomi-prices-colors.py
  python3 scripts/sync-oppo-xiaomi-prices-colors.py --dry-run
"""
from __future__ import annotations

import json
import os
import sys
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")

ATTR_COLOR = 6
SPU_XIAOMI = 7
SPU_OPPO = 8

XIAOMI_SUB = "Leica optics · Snapdragon 8 Gen 3 · 4610mAh battery"
OPPO_SUB = "Portrait-focused camera · sleek design"

# (sku_id, color, ram, capacity, price)
XIAOMI_ROWS = [
    (5, "Black", "8GB", "128GB", 799.0),
    (6, "Black", "8GB", "256GB", 899.0),
    (7, "White", "8GB", "128GB", 799.0),
    (8, "White", "8GB", "256GB", 899.0),
    (9, "Green", "8GB", "128GB", 799.0),
    (10, "Green", "8GB", "256GB", 899.0),
]

OPPO_ROWS = [
    (11, "Brown", "8GB", "256GB", 499.0),
    (12, "Brown", "12GB", "256GB", 579.0),
    (13, "White", "8GB", "256GB", 499.0),
    (14, "White", "12GB", "256GB", 579.0),
]

BROWN_IMAGE_SKU = 11


def api(method: str, path: str, body=None):
    url = f"{BASE}{path}"
    data = None
    headers = {}
    if body is not None:
        data = json.dumps(body).encode()
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode())


def build_xiaomi_copy(color: str, ram: str, capacity: str) -> tuple[str, str, str, str]:
    name = f"Xiaomi 14 · {ram} · {capacity} · {color}"
    title = f"Xiaomi 14 — {ram}+{capacity}, {color}"
    desc = f"Capacity:{capacity} · Color:{color} · RAM:{ram}"
    return name, title, XIAOMI_SUB, desc


def build_oppo_copy(color: str, ram: str, capacity: str) -> tuple[str, str, str, str]:
    name = f"OPPO Reno15 · {ram} · {capacity} · {color}"
    title = f"OPPO Reno15 — {ram}+{capacity}, {color}"
    desc = f"Capacity:{capacity} · Color:{color} · RAM:{ram}"
    return name, title, OPPO_SUB, desc


def update_color_sale_attr(sku_id: int, color: str, dry_run: bool):
    page = api("GET", "/product/skusaleattrvalue/list?page=1&limit=500")
    rows = [
        r
        for r in page["page"]["list"]
        if r.get("skuId") == sku_id and r.get("attrId") == ATTR_COLOR
    ]
    if not rows:
        raise RuntimeError(f"SKU {sku_id}: no Color row")
    row = rows[0]
    row["attrName"] = "Color"
    row["attrValue"] = color
    if not dry_run:
        api("POST", "/product/skusaleattrvalue/update", row)


def get_images(sku_id: int) -> list:
    return api("GET", f"/product/skuimages/bysku/{sku_id}").get("list", [])


def save_images_from_template(sku_id: int, template_sku_id: int, dry_run: bool) -> str | None:
    src = get_images(template_sku_id)
    if not src:
        return None
    images = [
        {
            "imgUrl": row["imgUrl"],
            "imgSort": i,
            "defaultImg": 1 if row.get("defaultImg") == 1 else 0,
        }
        for i, row in enumerate(src)
    ]
    if dry_run:
        return images[0]["imgUrl"]
    api("POST", "/product/skuimages/saveBatch", {"skuId": sku_id, "images": images})
    for row in images:
        if row.get("defaultImg") == 1:
            return row["imgUrl"]
    return images[0]["imgUrl"]


def apply_sku(
    sku_id: int,
    spu_id: int,
    color: str,
    ram: str,
    capacity: str,
    price: float,
    dry_run: bool,
    image_tpl: int | None = None,
):
    update_color_sale_attr(sku_id, color, dry_run)
    default_img = None
    if image_tpl is not None:
        default_img = save_images_from_template(sku_id, image_tpl, dry_run)

    if spu_id == SPU_XIAOMI:
        name, title, subtitle, desc = build_xiaomi_copy(color, ram, capacity)
    else:
        name, title, subtitle, desc = build_oppo_copy(color, ram, capacity)

    info = api("GET", f"/product/skuinfo/info/{sku_id}")["skuInfo"]
    info["skuName"] = name
    info["skuTitle"] = title
    info["skuSubtitle"] = subtitle
    info["skuDesc"] = desc
    info["price"] = price
    if default_img:
        info["skuDefaultImg"] = default_img

    if dry_run:
        print(f"[dry-run] SKU {sku_id}: {title} ${price}")
        return

    r = api("POST", "/product/skuinfo/update", info)
    print(f"SKU {sku_id}: ${price} searchSynced={r.get('searchSynced')} | {title}")


def main():
    dry_run = "--dry-run" in sys.argv
    for row in XIAOMI_ROWS:
        apply_sku(row[0], SPU_XIAOMI, row[1], row[2], row[3], row[4], dry_run)

    for row in OPPO_ROWS:
        tpl = BROWN_IMAGE_SKU if row[1] == "Brown" else None
        apply_sku(row[0], SPU_OPPO, row[1], row[2], row[3], row[4], dry_run, image_tpl=tpl)

    if not dry_run:
        for probe in (5, 11):
            item = api("GET", f"/product/item/{probe}")["item"]
            print(f"\nSPU item probe SKU {probe}:")
            for sa in item["saleAttr"]:
                vals = [v["attrValue"] for v in sa["attrValues"]]
                print(f"  {sa['attrName']}: {vals}")


if __name__ == "__main__":
    main()
