# GrainMart — Admin config fields → downstream contracts

Short contract note for how **product / warehouse configuration** written in admin (or product APIs) becomes the inputs that cart, order, ware, search, and seckill must honor.

**Why this exists:** configuration is not “UI-only.” Changing a SKU title, price, publish status, or stock has to stay consistent for every downstream consumer—same idea as software ↔ firmware field contracts on an embedded product line.

**Transport:** gateway `/api/**` → strip `/api`. Service-to-service calls use Spring `@HttpExchange` + load-balanced `RestClient` (not Feign). Catalog sync is **HTTP**; RabbitMQ is used for stock unlock / order release, **not** for pushing SPU/SKU config.

---

## 1. Configuration sources (admin / product)

| Config surface | Key fields | Owner | Publish / write APIs (gateway) |
|----------------|------------|-------|--------------------------------|
| **SPU** | `spuName`, `spuDescription`, `catalogId`, `brandId`, `weight`, `publishStatus` (`0` NEW, `1` UP, `2` DOWN) | `gulimall-product` | `POST /api/product/spuinfo/save`, `/update`, `/{spuId}/up`, `/{spuId}/down`, `/delete` |
| **SKU** | `skuId`, `spuId`, `skuName`, `skuTitle`, `skuSubtitle`, `skuDefaultImg`, **`price`**, `saleCount` | product | Nested in SPU save (`Skus`) or `/api/product/skuinfo/*` |
| **Base / sale attrs** | `attrId`, `attrName`, `attrValue(s)`, `searchType`, `attrType` | product | `/api/product/attr/*`, `POST /api/product/attr/update/{spuId}` |
| **Promotions (bounds / reductions)** | `buyBounds`, `growBounds`, ladder / full-reduction / member price fields | product → coupon | Written on SPU save via coupon HTTP (`saveSpuBounds`, `saveSkuReduction`) |
| **Warehouse stock** | `skuId`, `wareId`, **`stock`**, `stockLocked`, `skuName` | `gulimall-ware` | `/api/ware/waresku/save\|update\|delete`, `/syncFromProduct` |
| **Seckill relation** | `skuId`, session ids, `seckillPrice`, `seckillCount`, `seckillLimit` | coupon | `/api/coupon/seckillskurelation/*` |

Stock is **not** set on SPU save; it is owned by ware.

---

## 2. Downstream consumers (must honor config)

| Downstream | Fields / payload it depends on | How it reads | Failure / consistency notes |
|------------|--------------------------------|--------------|-----------------------------|
| **Product detail (storefront)** | Aggregated `SkuItemVo`: `info`, images, sale attrs, desc, group attrs | `GET /api/product/item/{skuId}`; Redis/local cache key `gulimall:product:item:{skuId}` | Cache evicted on SPU/SKU/desc changes |
| **Search (ES)** | `SkuEsModel`: `skuId`, `spuId`, `skuTitle`, `skuPrice`, `skuImg`, `hasStock`, brand/catalog, `attrs[]` | Written on SPU **up** / attr·sku refresh / ware stock notify → `SearchApi.productUp` / `down` | Unpublished SPUs must leave the index (`down`) |
| **Cart** | Snapshot: `skuTitle`, `skuDefaultImg`, `price`, sale-attr strings | HTTP `ProductApi` skuinfo + skusaleattrvalue → Redis cart | Cart holds a **snapshot**; price changes after add are a known product decision |
| **Order confirm / submit** | Live price + `OrderSkuMetaVo` (`skuId`, `spuId`, `spuName`, pic, brand, category); stock availability | HTTP product skuinfo / order meta; ware `hasStock` + **`/waresku/lock`** | Lock must succeed before order persistence; unlock = HTTP and/or MQ release |
| **Ware lock** | Own `stock` / `stockLocked` (not live product price) | Local DB; optional name sync from product | After stock change, HTTP notify product → search index refresh |
| **Seckill** | Warmup relation + display fields from product (`skuTitle`, img, `price`) | Coupon warmup → Redis; `ProductApi.info` for display | Quota / price are session-config, not free-form LLM output |

---

## 3. Lifecycle contract (happy path)

```text
Admin configures SPU+SKUs+attrs
        │
        ▼
POST /api/product/spuinfo/save
        │  HTTP → coupon bounds/reductions
        ▼
Ware admin sets stock (/api/ware/waresku/*)
        │
        ▼
POST /api/product/spuinfo/{spuId}/up
        │  build SkuEsModel (+ hasStock via WareApi)
        │  HTTP → Search productUp; publishStatus=1
        ▼
Storefront / search / cart / order / seckill consume via contracts above
```

**Down path:** `POST .../{spuId}/down` → search remove + `publishStatus=2`. Downstream list/search must not treat the SPU as sellable.

---

## 4. Integration checklist (config change → verify)

When changing a **configuration field**, treat it like a firmware register map: name the field, name the consumers, verify each.

| If you change… | Also verify… |
|----------------|--------------|
| `price` / title / image | Product item API + cache miss path; cart new adds; order confirm price |
| `publishStatus` up/down | ES document present/absent; search list; detail still gated if down |
| Sale / base attrs | Item page sale attrs; ES `attrs` filters |
| `stock` / `stockLocked` | `hasStock`, order lock, ES `hasStock`, seckill inventory assumptions |
| Seckill relation fields | Warmup Redis keys; kill path limits; display price |

---

## 5. Related code & docs

- Entities / VOs: `gulimall-product/.../entity/SpuInfoEntity.java`, `SkuInfoEntity.java`, `vo/SpuSaveVo.java`, `vo/SkuItemVo.java`
- ES model: `gulimall-common/.../to/es/SkuEsModel.java`
- Ware stock: `gulimall-ware/.../entity/WareSkuEntity.java`
- Broader architecture: [README.md](../README.md)
- Endpoint load paths: [load-test.md](./load-test.md)
