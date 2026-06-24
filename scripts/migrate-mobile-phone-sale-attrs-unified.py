#!/usr/bin/env python3
"""
Migrate Mobile Phones (catalog 225) SKU sale attrs to unified dimensions:
  Color (attrId=6), RAM (14), Capacity (8).

Replaces legacy Version / Memory rows. Then refreshes ES for on-sale SPUs.

Usage:
  GULIMALL_API=http://127.0.0.1:88/api python3 scripts/migrate-mobile-phone-sale-attrs-unified.py
  python3 scripts/migrate-mobile-phone-sale-attrs-unified.py --dry-run
"""
from __future__ import annotations

import argparse
import json
import os
import re
import urllib.error
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")

ATTR_COLOR = 6
ATTR_RAM = 14
ATTR_CAPACITY = 8

CATALOG_MOBILE = 225
SPU_IDS = [6, 7, 8, 10]


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


def parse_version(version: str) -> tuple[str, str]:
    """4+128G -> (4GB, 128GB)."""
    m = re.match(r"^(\d+)\+(\d+)\s*G?$", version.strip(), re.I)
    if not m:
        raise ValueError(f"cannot parse Version: {version!r}")
    return f"{m.group(1)}GB", f"{m.group(2)}GB"


def parse_memory(memory: str) -> str:
    m = re.match(r"^(\d+)\s*G$", memory.strip(), re.I)
    if not m:
        if memory.upper().endswith("GB"):
            return memory
        raise ValueError(f"cannot parse Memory: {memory!r}")
    return f"{m.group(1)}GB"


def normalize_gb(value: str) -> str:
    v = value.strip()
    if re.match(r"^\d+GB$", v, re.I):
        return v[:-2] + "GB" if v[-2:].lower() == "gb" else v
    if re.match(r"^\d+G$", v, re.I):
        return v[:-1] + "GB"
    return v


def to_unified(attr_map: dict[str, str], sku_id: int) -> list[dict]:
    color = attr_map.get("Color")
    if not color:
        raise ValueError(f"SKU {sku_id}: missing Color")

    ram = attr_map.get("RAM")
    cap = attr_map.get("Capacity")

    if "Version" in attr_map:
        ram, cap = parse_version(attr_map["Version"])
    elif "Memory" in attr_map:
        ram = parse_memory(attr_map["Memory"])
        # Reno15 demo: 8G/12G RAM variants share 256GB storage
        cap = cap or "256GB"
    elif ram and cap:
        ram = normalize_gb(ram)
        cap = normalize_gb(cap) if not cap.upper().endswith("GB") else cap
    else:
        raise ValueError(f"SKU {sku_id}: need Version/Memory or RAM+Capacity, got {attr_map}")

    ram = normalize_gb(ram)
    if not cap.upper().endswith("GB"):
        cap = normalize_gb(cap)

    return [
        {"skuId": sku_id, "attrId": ATTR_COLOR, "attrName": "Color", "attrValue": color, "attrSort": 0},
        {"skuId": sku_id, "attrId": ATTR_RAM, "attrName": "RAM", "attrValue": ram, "attrSort": 1},
        {"skuId": sku_id, "attrId": ATTR_CAPACITY, "attrName": "Capacity", "attrValue": cap, "attrSort": 2},
    ]


def list_sale_rows_for_sku(sku_id: int) -> list[dict]:
    page = api("GET", f"/product/skusaleattrvalue/list?page=1&limit=200")
    rows = page.get("page", {}).get("list", [])
    return [r for r in rows if r.get("skuId") == sku_id]


def delete_rows(rows: list[dict], dry_run: bool):
    ids = [r["id"] for r in rows if r.get("id") is not None]
    if not ids:
        return
    if dry_run:
        print(f"    would delete ids {ids}")
        return
    api("POST", "/product/skusaleattrvalue/delete", ids)


def save_rows(rows: list[dict], dry_run: bool):
    for row in rows:
        if dry_run:
            print(f"    would save {row}")
            continue
        api("POST", "/product/skusaleattrvalue/save", row)


def refresh_search(spu_id: int, sku_ids: list[int], dry_run: bool):
    """Trigger search sync via sku update on one sku per spu (Plan B hook)."""
    if dry_run or not sku_ids:
        return
    sid = sku_ids[0]
    info = api("GET", f"/product/skuinfo/info/{sid}")
    sku = info["skuInfo"]
    r = api("POST", "/product/skuinfo/update", sku)
    print(f"  ES refresh via SKU {sid}: searchSynced={r.get('searchSynced')}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    page = api("GET", "/product/skuinfo/list?page=1&limit=100")
    skus = [s for s in page.get("page", {}).get("list", []) if s.get("catalogId") == CATALOG_MOBILE]
    skus = [s for s in skus if s.get("spuId") in SPU_IDS]
    skus.sort(key=lambda x: x["skuId"])

    print(f"Migrating {len(skus)} SKUs under SPUs {SPU_IDS} (catalog {CATALOG_MOBILE})")
    by_spu: dict[int, list[int]] = {}
    for s in skus:
        sid = s["skuId"]
        spu_id = s["spuId"]
        strings = api("GET", f"/product/skusaleattrvalue/strings/{sid}").get("data", [])
        attr_map = parse_strings(strings)
        print(f"SKU {sid} (SPU {spu_id}) before: {strings}")
        try:
            unified = to_unified(attr_map, sid)
        except ValueError as e:
            print(f"  SKIP: {e}")
            continue
        old_rows = list_sale_rows_for_sku(sid)
        delete_rows(old_rows, args.dry_run)
        save_rows(unified, args.dry_run)
        after = [f"{r['attrName']}:{r['attrValue']}" for r in unified]
        print(f"  after: {after}")
        by_spu.setdefault(spu_id, []).append(sid)

    print("\nRe-run sku copy script + ES sync recommended.")
    for spu_id, sku_ids in sorted(by_spu.items()):
        refresh_search(spu_id, sku_ids, args.dry_run)

    if not args.dry_run:
        print("\nRun: python3 docs/scripts/update-sku-copy.py")


if __name__ == "__main__":
    main()
