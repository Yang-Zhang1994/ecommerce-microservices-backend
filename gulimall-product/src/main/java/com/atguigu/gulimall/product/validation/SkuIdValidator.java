package com.atguigu.gulimall.product.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cheap first-line guard against cache-penetration via bogus SKU ids: rejects ids that are clearly
 * out of any plausible range (null, non-positive, or absurdly large) before any cache/DB lookup.
 *
 * <p>This is intentionally a coarse range/format check, not an existence check — it costs nothing
 * and filters obvious garbage; a Bloom filter would be the next layer for "looks valid but does
 * not exist".</p>
 *
 * <p>Bounds are bound from {@code gulimall.product.sku-id.*} with generous defaults so legitimate
 * ids are never blocked.</p>
 */
@Component
@ConfigurationProperties(prefix = "gulimall.product.sku-id")
public class SkuIdValidator {

    /** Smallest acceptable id (auto-increment ids start at 1). */
    private long min = 1L;

    /** Largest plausible id; anything beyond is treated as obviously bogus. */
    private long max = 1_000_000_000_000L;

    public boolean isPlausible(Long skuId) {
        return skuId != null && skuId >= min && skuId <= max;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
}
