#!/usr/bin/env python3
"""
Sync sku_name / sku_title / sku_subtitle with sale attributes (exact attr values).
Subtitle uses SPU marketing line only; variant text always comes from pms_sku_sale_attr_value.
"""
import json
import os
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://localhost:88/api").rstrip("/")

SPU_SUBTITLE = {
    6: "A17 Pro chip · titanium design · ProMotion display",
    7: "Leica optics · Snapdragon 8 Gen 3 · 4610mAh battery",
    8: "Portrait-focused camera · sleek design",
    10: '6.7" Super AMOLED 120Hz · Exynos 1380 · IP67 5G',
}

# brand_id -> display name for search title
BRAND_NAMES = {1: "OPPO", 4: "Xiaomi", 5: "Apple", 8: "Samsung"}

# spu_id -> short label for admin sku_name (without brand prefix)
SPU_SHORT = {
    6: "iPhone 15 Pro",
    7: "Xiaomi 14",
    8: "OPPO Reno15",
    10: "Galaxy A36 5G",
}


def api(method: str, path: str, body=None):
    url = f"{BASE}{path}"
    data = None
    headers = {}
    if body is not None:
        data = json.dumps(body).encode()
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode())


def parse_sale_attrs(strings):
    out = {}
    for s in strings or []:
        if ":" not in s:
            continue
        k, v = s.split(":", 1)
        out[k.strip()] = v.strip()
    return out


def variant_parts(attr_map):
    """Catalog 225 order: RAM, Capacity, Color (legacy Version/Memory still supported)."""
    if "Version" in attr_map:
        spec = [attr_map["Version"]]
        if "Color" in attr_map:
            spec.append(attr_map["Color"])
        return spec
    if "Memory" in attr_map:
        spec = [attr_map["Memory"]]
        if "Color" in attr_map:
            spec.append(attr_map["Color"])
        return spec
    parts = []
    for key in ("RAM", "Capacity", "Color"):
        if key in attr_map and attr_map[key]:
            parts.append(attr_map[key])
    return parts


def join_spec_for_title(parts):
    """Build title spec segment from exact attr values."""
    if not parts:
        return ""
    if len(parts) == 1:
        return parts[0]
    if len(parts) == 3:
        return f"{parts[0]}+{parts[1]}, {parts[2]}"
    return f"{parts[0]}, {parts[1]}"


def build_copy(sku_row, spu_info, sale_strings):
    spu_id = sku_row["spuId"]
    brand_id = sku_row.get("brandId") or spu_info.get("brandId")
    brand = BRAND_NAMES.get(brand_id, "")
    spu_name = spu_info.get("spuName") or SPU_SHORT.get(spu_id, "")
    short = SPU_SHORT.get(spu_id, spu_name)
    subtitle = SPU_SUBTITLE.get(spu_id, (spu_info.get("spuDescription") or "")[:120])

    attr_map = parse_sale_attrs(sale_strings)
    parts = variant_parts(attr_map)
    if not parts:
        raise ValueError(f"SKU {sku_row['skuId']}: no sale attributes")

    sku_name = f"{short} · " + " · ".join(parts)
    spec = join_spec_for_title(parts)
    if brand and spu_name.lower().startswith(brand.lower()):
        title_head = spu_name
    elif brand:
        title_head = f"{brand} {spu_name}"
    else:
        title_head = spu_name
    sku_title = f"{title_head} — {spec}"
    sku_desc = " · ".join(f"{k}:{v}" for k, v in sorted(attr_map.items()))

    return sku_name, sku_title, subtitle, sku_desc


def main():
    page = api("GET", "/product/skuinfo/list?page=1&limit=100")
    skus = page.get("page", {}).get("list", [])
    spu_cache = {}

    for row in sorted(skus, key=lambda x: x["skuId"]):
        sku_id = row["skuId"]
        spu_id = row["spuId"]
        if spu_id not in spu_cache:
            spu_cache[spu_id] = api("GET", f"/product/spuinfo/info/{spu_id}")["spuInfo"]

        sale = api("GET", f"/product/skusaleattrvalue/strings/{sku_id}").get("data", [])
        name, title, subtitle, desc = build_copy(row, spu_cache[spu_id], sale)

        info = api("GET", f"/product/skuinfo/info/{sku_id}")
        sku = info["skuInfo"]
        sku["skuName"] = name
        sku["skuTitle"] = title
        sku["skuSubtitle"] = subtitle
        sku["skuDesc"] = desc
        result = api("POST", "/product/skuinfo/update", sku)
        print(f"SKU {sku_id} sale={sale}")
        print(f"  name={name}")
        print(f"  title={title}")
        print(f"  synced={result.get('searchSynced')}")
        print()


if __name__ == "__main__":
    main()
