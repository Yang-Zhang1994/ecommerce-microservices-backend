#!/usr/bin/env python3
"""
Xiaomi 14 (SPU 7, brand 4):
  - Black SKUs → images from SKU 5
  - White SKUs → images from SKU 7
  - Blue SKUs  → rename to Green (title + sale attrs), images from SKU 9

Usage:
  GULIMALL_API=http://127.0.0.1:88/api python3 scripts/sync-xiaomi-sku-images-colors.py
  python3 scripts/sync-xiaomi-sku-images-colors.py --dry-run
"""
from __future__ import annotations

import json
import os
import sys
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")

SPU_ID = 7
BRAND_ID = 4
ATTR_COLOR = 6
BLACK_IMAGE_SKU = 5
WHITE_IMAGE_SKU = 7
GREEN_IMAGE_SKU = 9
XIAOMI_SUB = "Leica optics · Snapdragon 8 Gen 3 · 4610mAh battery"


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


def parse_sale_attrs(strings: list[str]) -> dict[str, str]:
    out = {}
    for s in strings or []:
        if ":" not in s:
            continue
        k, v = s.split(":", 1)
        out[k.strip()] = v.strip()
    return out


def build_copy(color: str, ram: str, capacity: str) -> tuple[str, str, str, str]:
    name = f"Xiaomi 14 · {ram} · {capacity} · {color}"
    title = f"Xiaomi 14 — {ram}+{capacity}, {color}"
    desc = f"Capacity:{capacity} · Color:{color} · RAM:{ram}"
    return name, title, XIAOMI_SUB, desc


def get_images(sku_id: int) -> list:
    return api("GET", f"/product/skuimages/bysku/{sku_id}").get("list", [])


def save_images_from_template(sku_id: int, template_sku_id: int, dry_run: bool) -> str | None:
    src = get_images(template_sku_id)
    if not src:
        raise RuntimeError(f"Template SKU {template_sku_id} has no images")
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


def update_color_sale_attr(sku_id: int, color: str, dry_run: bool):
    page = api("GET", "/product/skusaleattrvalue/list?page=1&limit=500")
    color_rows = [
        r
        for r in page["page"]["list"]
        if r.get("skuId") == sku_id and r.get("attrId") == ATTR_COLOR
    ]
    if not color_rows:
        raise RuntimeError(f"SKU {sku_id}: no Color sale attr row")
    row = color_rows[0]
    row["attrName"] = "Color"
    row["attrValue"] = color
    if not dry_run:
        api("POST", "/product/skusaleattrvalue/update", row)


def resolve_color_and_template(old_color: str) -> tuple[str, int]:
    if old_color == "Black":
        return "Black", BLACK_IMAGE_SKU
    if old_color == "White":
        return "White", WHITE_IMAGE_SKU
    if old_color in ("Blue", "Green"):
        return "Green", GREEN_IMAGE_SKU
    raise ValueError(f"unsupported color {old_color!r}")


def main():
    dry_run = "--dry-run" in sys.argv
    page = api("GET", "/product/skuinfo/list?page=1&limit=200")
    skus = sorted(
        [
            s
            for s in page["page"]["list"]
            if s.get("spuId") == SPU_ID and s.get("brandId") == BRAND_ID
        ],
        key=lambda s: s["skuId"],
    )
    if not skus:
        print("No Xiaomi SKUs found for SPU 7")
        return

    for s in skus:
        sku_id = s["skuId"]
        strings = api("GET", f"/product/skusaleattrvalue/strings/{sku_id}").get("data", [])
        attr_map = parse_sale_attrs(strings)
        old_color = attr_map.get("Color", "")
        ram = attr_map.get("RAM", "")
        capacity = attr_map.get("Capacity", "")
        if not ram or not capacity:
            print(f"SKU {sku_id}: skip (missing RAM/Capacity)")
            continue

        color, img_tpl = resolve_color_and_template(old_color)
        if old_color != color:
            update_color_sale_attr(sku_id, color, dry_run)
        default_img = save_images_from_template(sku_id, img_tpl, dry_run)

        name, title, subtitle, desc = build_copy(color, ram, capacity)
        info = api("GET", f"/product/skuinfo/info/{sku_id}")["skuInfo"]
        info["skuName"] = name
        info["skuTitle"] = title
        info["skuSubtitle"] = subtitle
        info["skuDesc"] = desc
        if default_img:
            info["skuDefaultImg"] = default_img

        if dry_run:
            print(
                f"[dry-run] SKU {sku_id}: {old_color} -> {color}, "
                f"images<=SKU {img_tpl}, {name}"
            )
            continue

        r = api("POST", "/product/skuinfo/update", info)
        print(
            f"SKU {sku_id}: color {old_color!r} -> {color!r}, images from SKU {img_tpl}, "
            f"searchSynced={r.get('searchSynced')}"
        )
        print(f"  {title}")

    if not dry_run:
        item = api("GET", f"/product/item/{BLACK_IMAGE_SKU}")["item"]
        print("\nDetail sale attrs:")
        for row in item["saleAttr"]:
            vals = [(v["attrValue"], v["skuIds"]) for v in row["attrValues"]]
            print(f"  {row['attrName'].strip()}: {vals}")


if __name__ == "__main__":
    main()
