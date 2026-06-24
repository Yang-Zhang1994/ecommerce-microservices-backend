#!/usr/bin/env python3
"""SPU 6 (iPhone 15 Pro): only Blue + White; Blue images=SKU1, White images=SKU3."""
import json
import os
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://localhost:88/api").rstrip("/")

SPU_ID = 6
BLUE_IMAGE_SKU = 1
WHITE_IMAGE_SKU = 3
ATTR_COLOR = 6
IPHONE_SUB = "A17 Pro chip · titanium design · ProMotion display"


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


def parse_sale_attrs(strings):
    out = {}
    for s in strings or []:
        if ":" not in s:
            continue
        k, v = s.split(":", 1)
        out[k.strip()] = v.strip()
    return out


def build_copy(color: str, ram: str, capacity: str) -> tuple[str, str, str, str]:
    name = f"iPhone 15 Pro · {ram} · {capacity} · {color}"
    title = f"Apple iPhone 15 Pro — {ram}+{capacity}, {color}"
    desc = f"Capacity:{capacity} · Color:{color} · RAM:{ram}"
    return name, title, IPHONE_SUB, desc


def get_images(sku_id: int) -> list:
    return api("GET", f"/product/skuimages/bysku/{sku_id}").get("list", [])


def save_images_from_template(sku_id: int, template_sku_id: int) -> str | None:
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
    api("POST", "/product/skuimages/saveBatch", {"skuId": sku_id, "images": images})
    for row in images:
        if row.get("defaultImg") == 1:
            return row["imgUrl"]
    return images[0]["imgUrl"]


def update_color_sale_attr(sku_id: int, color: str):
    page = api("GET", "/product/skusaleattrvalue/list?page=1&limit=500")
    color_rows = [
        r
        for r in page["page"]["list"]
        if r.get("skuId") == sku_id and r.get("attrId") == ATTR_COLOR
    ]
    if not color_rows:
        raise RuntimeError(f"SKU {sku_id}: no Color row")
    row = color_rows[0]
    row["attrName"] = "Color"
    row["attrValue"] = color
    api("POST", "/product/skusaleattrvalue/update", row)


def main():
    page = api("GET", "/product/skuinfo/list?page=1&limit=100")
    skus = sorted(
        [s for s in page["page"]["list"] if s.get("spuId") == SPU_ID],
        key=lambda s: s["skuId"],
    )

    for s in skus:
        sku_id = s["skuId"]
        strings = api("GET", f"/product/skusaleattrvalue/strings/{sku_id}").get("data", [])
        attr_map = parse_sale_attrs(strings)
        old_color = attr_map.get("Color", "")
        ram = attr_map.get("RAM", "")
        capacity = attr_map.get("Capacity", "")

        if old_color in ("Red", "Blue"):
            color = "Blue"
            img_tpl = BLUE_IMAGE_SKU
        elif old_color == "White":
            color = "White"
            img_tpl = WHITE_IMAGE_SKU
        else:
            print(f"SKU {sku_id}: skip color {old_color!r}")
            continue

        update_color_sale_attr(sku_id, color)
        default_img = save_images_from_template(sku_id, img_tpl)

        name, title, subtitle, desc = build_copy(color, ram, capacity)
        info = api("GET", f"/product/skuinfo/info/{sku_id}")["skuInfo"]
        info["skuName"] = name
        info["skuTitle"] = title
        info["skuSubtitle"] = subtitle
        info["skuDesc"] = desc
        if default_img:
            info["skuDefaultImg"] = default_img

        r = api("POST", "/product/skuinfo/update", info)
        print(
            f"SKU {sku_id}: {old_color} -> {color}, images from SKU {img_tpl}, "
            f"searchSynced={r.get('searchSynced')}"
        )
        print(f"  {name}")

    print("\nSale attr options on detail page:")
    item = api("GET", f"/product/item/{BLUE_IMAGE_SKU}")["item"]
    for row in item["saleAttr"]:
        vals = [(v["attrValue"], v["skuIds"]) for v in row["attrValues"]]
        print(f"  {row['attrName'].strip()}: {vals}")


if __name__ == "__main__":
    main()
