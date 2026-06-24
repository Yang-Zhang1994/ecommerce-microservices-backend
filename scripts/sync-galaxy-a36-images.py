#!/usr/bin/env python3
"""Galaxy A36 5G (SPU 10): all SKUs use images from SKU 15."""
from __future__ import annotations

import json
import os
import sys
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")
SPU_ID = 10
IMAGE_TEMPLATE_SKU = 15


def api(method: str, path: str, body=None):
    url = f"{BASE}{path}"
    data = json.dumps(body).encode() if body else None
    h = {"Content-Type": "application/json"} if body else {}
    req = urllib.request.Request(url, data=data, headers=h, method=method)
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode())


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
        for row in images:
            if row.get("defaultImg") == 1:
                return row["imgUrl"]
        return images[0]["imgUrl"]
    api("POST", "/product/skuimages/saveBatch", {"skuId": sku_id, "images": images})
    for row in images:
        if row.get("defaultImg") == 1:
            return row["imgUrl"]
    return images[0]["imgUrl"]


def main():
    dry_run = "--dry-run" in sys.argv
    page = api("GET", "/product/skuinfo/list?page=1&limit=500")
    skus = sorted(
        [s for s in page["page"]["list"] if s.get("spuId") == SPU_ID],
        key=lambda s: s["skuId"],
    )
    if not skus:
        print(f"No SKUs for SPU {SPU_ID}")
        return

    for s in skus:
        sku_id = s["skuId"]
        default_img = save_images_from_template(sku_id, IMAGE_TEMPLATE_SKU, dry_run)
        if dry_run:
            print(f"[dry-run] SKU {sku_id}: images <= SKU {IMAGE_TEMPLATE_SKU}")
            continue
        info = api("GET", f"/product/skuinfo/info/{sku_id}")["skuInfo"]
        if default_img:
            info["skuDefaultImg"] = default_img
        r = api("POST", "/product/skuinfo/update", info)
        print(
            f"SKU {sku_id}: {len(get_images(sku_id))} images from SKU {IMAGE_TEMPLATE_SKU}, "
            f"searchSynced={r.get('searchSynced')}"
        )


if __name__ == "__main__":
    main()
