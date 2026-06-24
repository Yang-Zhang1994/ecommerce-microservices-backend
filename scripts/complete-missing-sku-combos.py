#!/usr/bin/env python3
"""
Create SKUs for missing Color x RAM x Capacity combinations (catalog 225).

Usage:
  GULIMALL_API=http://127.0.0.1:88/api python3 scripts/complete-missing-sku-combos.py
  python3 scripts/complete-missing-sku-combos.py --spu-id 6
  python3 scripts/complete-missing-sku-combos.py --dry-run
"""
from __future__ import annotations

import argparse
import itertools
import json
import os
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")

ATTR_COLOR = 6
ATTR_RAM = 14
ATTR_CAPACITY = 8

SPU_SHORT = {
    6: "iPhone 15 Pro",
    7: "Xiaomi 14",
    8: "OPPO Reno15",
    10: "Galaxy A36 5G",
}
BRAND_PREFIX = {5: "Apple", 4: "Xiaomi", 1: "OPPO", 8: "Samsung"}
SPU_SUBTITLE = {
    6: "A17 Pro chip · titanium design · ProMotion display",
    7: "Leica optics · Snapdragon 8 Gen 3 · 4610mAh battery",
    8: "Portrait-focused camera · sleek design",
    10: '6.7" Super AMOLED 120Hz · Exynos 1380 · IP67 5G',
}


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


def parse_strings(strings: list[str]) -> dict[str, str]:
    out = {}
    for s in strings or []:
        if ":" not in s:
            continue
        k, v = s.split(":", 1)
        out[k.strip()] = v.strip()
    return out


def build_copy(spu_id: int, brand_id: int, color: str, ram: str, capacity: str) -> tuple[str, str, str, str]:
    short = SPU_SHORT.get(spu_id, f"SPU{spu_id}")
    brand = BRAND_PREFIX.get(brand_id, "")
    spu_name = short
    name = f"{short} · {ram} · {capacity} · {color}"
    title_head = f"{brand} {spu_name}".strip() if brand and not spu_name.lower().startswith((brand or "").lower()) else spu_name
    title = f"{title_head} — {ram}+{capacity}, {color}"
    subtitle = SPU_SUBTITLE.get(spu_id, "")
    desc = f"Capacity:{capacity} · Color:{color} · RAM:{ram}"
    return name, title, subtitle, desc


def price_from_template(template_price: float, tpl: tuple[str, str, str], target: tuple[str, str, str]) -> float:
    """Adjust price when RAM/Capacity differ from template."""
    _, tram, tcap = tpl
    _, ram, cap = target
    price = float(template_price)
    ram_gb = int(ram.replace("GB", ""))
    tram_gb = int(tram.replace("GB", ""))
    if ram_gb > tram_gb:
        price += 200 * (ram_gb - tram_gb) // 4
    elif ram_gb < tram_gb:
        price -= 100
    cap_gb = int(cap.replace("GB", ""))
    tcap_gb = int(tcap.replace("GB", ""))
    if cap_gb > tcap_gb:
        price += 150 * (cap_gb - tcap_gb) // 128
    elif cap_gb < tcap_gb:
        price -= 100
    return max(price, 99.0)


def pick_template(existing: dict[int, tuple[str, str, str]], color: str, ram: str, cap: str) -> int:
    best_id = None
    best_score = -1
    for sku_id, (c, r, cp) in existing.items():
        score = (color == c) * 4 + (ram == r) * 2 + (cap == cp) * 1
        if score > best_score:
            best_score = score
            best_id = sku_id
    return best_id


def create_sku_for_combo(
    spu_id: int,
    template_sku_id: int,
    color: str,
    ram: str,
    capacity: str,
    dry_run: bool,
) -> int | None:
    tpl_info = api("GET", f"/product/skuinfo/info/{template_sku_id}")["skuInfo"]
    tpl_attrs = parse_strings(
        api("GET", f"/product/skusaleattrvalue/strings/{template_sku_id}").get("data", [])
    )
    tpl_combo = (tpl_attrs["Color"], tpl_attrs["RAM"], tpl_attrs["Capacity"])
    target = (color, ram, capacity)

    name, title, subtitle, desc = build_copy(
        spu_id, tpl_info["brandId"], color, ram, capacity
    )
    price = price_from_template(float(tpl_info["price"]), tpl_combo, target)

    sku_body = {
        "spuId": spu_id,
        "skuName": name,
        "skuTitle": title,
        "skuSubtitle": subtitle,
        "skuDesc": desc,
        "catalogId": tpl_info["catalogId"],
        "brandId": tpl_info["brandId"],
        "skuDefaultImg": tpl_info["skuDefaultImg"],
        "price": price,
        "saleCount": 0,
    }

    if dry_run:
        print(f"  would create: {target} from SKU {template_sku_id} price={price:.2f}")
        return None

    saved = api("POST", "/product/skuinfo/save", sku_body)
    new_id = saved.get("skuInfo", {}).get("skuId")
    if not new_id:
        page = api("GET", f"/product/skuinfo/list?page=1&limit=200&spuId={spu_id}")
        for row in page.get("page", {}).get("list", []):
            if row.get("skuName") == name:
                new_id = row["skuId"]
                break
    if not new_id:
        raise RuntimeError(f"save did not return skuId: {saved}")

    for sort, (attr_id, attr_name, val) in enumerate(
        [(ATTR_COLOR, "Color", color), (ATTR_RAM, "RAM", ram), (ATTR_CAPACITY, "Capacity", capacity)]
    ):
        api(
            "POST",
            "/product/skusaleattrvalue/save",
            {
                "skuId": new_id,
                "attrId": attr_id,
                "attrName": attr_name,
                "attrValue": val,
                "attrSort": sort,
            },
        )

    img_list = api("GET", f"/product/skuimages/bysku/{template_sku_id}").get("list", [])
    if img_list:
        images = [
            {
                "imgUrl": row["imgUrl"],
                "imgSort": i,
                "defaultImg": 1 if row.get("defaultImg") == 1 else 0,
            }
            for i, row in enumerate(img_list)
        ]
        api(
            "POST",
            "/product/skuimages/saveBatch",
            {"skuId": new_id, "images": images},
        )

    # Warehouse stock (clone default warehouse row if present)
    try:
        ware_page = api("GET", f"/ware/waresku/list?page=1&limit=20&skuId={template_sku_id}")
        ware_rows = ware_page.get("page", {}).get("list", [])
        if ware_rows:
            w = ware_rows[0]
            api(
                "POST",
                "/ware/waresku/save",
                {
                    "skuId": new_id,
                    "wareId": w["wareId"],
                    "stock": w.get("stock", 30),
                    "skuName": name,
                    "stockLocked": 0,
                },
            )
    except Exception as e:
        print(f"    ware stock skip: {e}")

    api("POST", "/product/skuinfo/update", api("GET", f"/product/skuinfo/info/{new_id}")["skuInfo"])
    print(f"  created SKU {new_id}: {color} / {ram} / {capacity}  price={price:.2f}")
    return new_id


def process_spu(spu_id: int, dry_run: bool) -> int:
    page = api("GET", "/product/skuinfo/list?page=1&limit=200")
    spu_skus = [s for s in page["page"]["list"] if s.get("spuId") == spu_id]
    if not spu_skus:
        print(f"SPU {spu_id}: no SKUs")
        return 0

    item = api("GET", f"/product/item/{spu_skus[0]['skuId']}")
    sale = item["item"]["saleAttr"]
    dims = {row["attrName"].strip(): [v["attrValue"] for v in row["attrValues"]] for row in sale}
    if not all(k in dims for k in ("Color", "RAM", "Capacity")):
        print(f"SPU {spu_id}: not on unified Color/RAM/Capacity attrs")
        return 0

    existing: dict[int, tuple[str, str, str]] = {}
    for s in spu_skus:
        attrs = parse_strings(api("GET", f"/product/skusaleattrvalue/strings/{s['skuId']}")["data"])
        existing[s["skuId"]] = (attrs["Color"], attrs["RAM"], attrs["Capacity"])

    combos = set(existing.values())
    expected = set(itertools.product(dims["Color"], dims["RAM"], dims["Capacity"]))
    missing = sorted(expected - combos)
    if not missing:
        print(f"SPU {spu_id}: already complete ({len(combos)} SKUs)")
        return 0

    spu_name = api("GET", f"/product/spuinfo/info/{spu_id}")["spuInfo"]["spuName"]
    print(f"SPU {spu_id} ({spu_name}): adding {len(missing)} SKU(s)")
    created = 0
    for color, ram, cap in missing:
        tpl_id = pick_template(existing, color, ram, cap)
        new_id = create_sku_for_combo(spu_id, tpl_id, color, ram, cap, dry_run)
        if new_id:
            existing[new_id] = (color, ram, cap)
            created += 1
    return created


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--spu-id", type=int, action="append", dest="spu_ids")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    spu_ids = args.spu_ids or [6, 7, 8, 10]
    total = 0
    for spu_id in spu_ids:
        total += process_spu(spu_id, args.dry_run)
    print(f"\nDone. Created {total} SKU(s).")
    if total and not args.dry_run:
        print("Run: python3 docs/scripts/update-sku-copy.py  # optional name polish")


if __name__ == "__main__":
    main()
