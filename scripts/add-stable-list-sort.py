#!/usr/bin/env python3
"""Add stable id ASC sort to JPA queryPage calls that lack explicit sort."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATTERN = re.compile(
    r"new Query<(\w+)>\(\)\.getPageable\(params\);"
)

SKIP_FILES = {
    "OrderServiceImpl.java",  # member-facing list uses createTime elsewhere
}


def patch_file(path: Path) -> bool:
    if path.name in SKIP_FILES:
        return False
    text = path.read_text(encoding="utf-8")
    if "getPageable(params, Sort.by" in text and "getPageable(params);" not in text:
        return False
    if "getPageable(params);" not in text:
        return False
    new_text = PATTERN.sub(
        r'new Query<\1>().getPageable(params, Sort.by("id").ascending());',
        text,
    )
    if new_text == text:
        return False
    if "import org.springframework.data.domain.Sort;" not in new_text:
        new_text = new_text.replace(
            "import org.springframework.data.domain.Pageable;",
            "import org.springframework.data.domain.Pageable;\nimport org.springframework.data.domain.Sort;",
        )
    path.write_text(new_text, encoding="utf-8")
    return True


def main() -> None:
    changed = []
    for path in ROOT.glob("gulimall-*/src/main/java/**/*ServiceImpl.java"):
        if patch_file(path):
            changed.append(path.relative_to(ROOT))
    # Order admin list: stable by id (edit does not reorder)
    order_path = ROOT / "gulimall-order/src/main/java/com/atguigu/gulimall/order/service/impl/OrderServiceImpl.java"
    if order_path.exists():
        text = order_path.read_text(encoding="utf-8")
        old = "Pageable pageable = new Query<OrderEntity>().getPageable(params);"
        new = 'Pageable pageable = new Query<OrderEntity>().getPageable(params, Sort.by("id").ascending());'
        if old in text:
            text = text.replace(old, new)
            order_path.write_text(text, encoding="utf-8")
            changed.append(order_path.relative_to(ROOT))
    print(f"Patched {len(changed)} service files")
    for p in changed:
        print(f"  - {p}")


if __name__ == "__main__":
    main()
