# Marketing admin menu visibility (GrainMart)

Sidebar filtering is implemented in `renren-fast-vue/src/config/adminMenuVisibility.js` and applied on login via `router/index.js` → `filterUnfinishedMenus` (after English menu labels are applied).

**Rule:** any `coupon/*` menu is **hidden by default** unless its **name** or **URL** is on the allowlist below. This covers DB paths like `coupon/history`, `coupon/subject`, `coupon/seckill` as well as longer paths.

## Hidden (should not appear) — includes your first three

| Sidebar (EN) | Should hide? |
|--------------|--------------|
| **Issue History** | Yes |
| **Subject** | Yes |
| **Seckill** | Yes |
| Coupons, Daily Flash Sale, etc. | Yes |

## Visible with notice (only these three)

Pages show `MarketingStorefrontNotice`: *Stored for future use; not applied on the storefront yet.*

| Sidebar (EN) | Typical URL |
|--------------|-------------|
| **Bounds** (Points) | `coupon/spubounds` |
| **Full Discount** | `coupon/skufullreduction` |
| **Member Price** | `coupon/memberprice` |

After changing this list, **log out and log in** (or hard refresh) so `/sys/menu/nav` is re-filtered.
