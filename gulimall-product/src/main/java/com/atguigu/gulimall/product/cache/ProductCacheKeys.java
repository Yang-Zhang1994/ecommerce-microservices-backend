package com.atguigu.gulimall.product.cache;

/** Central place for product cache key names so producers and evictors stay in sync. */
public final class ProductCacheKeys {

    private ProductCacheKeys() {
    }

    private static final String ITEM_PREFIX = "gulimall:product:item:";

    /** Aggregated product-detail VO for one SKU (used by {@code /api/product/item/{skuId}}). */
    public static String item(Long skuId) {
        return ITEM_PREFIX + skuId;
    }
}
