#!/usr/bin/env python3
"""
Refresh wms_ware_sku rows: sku_name from product catalog, stock_locked defaults to 0.
Uses existing list/info/update APIs (no new ware endpoint required).
"""
from __future__ import annotations

import json
import os
import sys
import urllib.request

BASE = os.environ.get("GULIMALL_API", "http://127.0.0.1:88/api").rstrip("/")


def api(method: str, path: str, body=None):
    url = f"{BASE}{path}"
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"} if body is not None else {}
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode())


def sku_display_name(sku_info: dict) -> str | None:
    for key in ("skuName", "skuTitle"):
        val = sku_info.get(key)
        if val and str(val).strip():
            return str(val).strip()
    return None


def main() -> None:
    dry_run = "--dry-run" in sys.argv
    page = api("GET", "/ware/waresku/list?page=1&limit=500")
    rows = page.get("page", {}).get("list", [])
    updated = 0
    for row in rows:
        wid = row["id"]
        sku_id = row["skuId"]
        locked = row.get("stockLocked")
        if locked is None:
            locked = 0
        try:
            info = api("GET", f"/product/skuinfo/info/{sku_id}")
            sku_info = info.get("skuInfo") or {}
        except Exception as e:
            print(f"SKIP id={wid} sku_id={sku_id}: product info failed ({e})")
            continue
        name = sku_display_name(sku_info)
        if not name:
            print(f"SKIP id={wid} sku_id={sku_id}: no skuName in product")
            continue
        payload = {
            "id": wid,
            "skuId": sku_id,
            "wareId": row.get("wareId"),
            "stock": row.get("stock"),
            "skuName": name,
            "stockLocked": locked,
        }
        if row.get("skuName") == name and row.get("stockLocked") == locked:
            continue
        if dry_run:
            print(f"[dry-run] id={wid} sku_id={sku_id}: {row.get('skuName')!r} -> {name!r}, locked={locked}")
        else:
            api("POST", "/ware/waresku/update", payload)
            print(f"OK id={wid} sku_id={sku_id}: {name!r}, locked={locked}")
        updated += 1
    print(f"Done. {'Would update' if dry_run else 'Updated'} {updated} row(s).")


if __name__ == "__main__":
    main()
