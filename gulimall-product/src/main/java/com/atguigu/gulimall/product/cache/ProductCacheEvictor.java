package com.atguigu.gulimall.product.cache;

import com.atguigu.common.cache.ProtectedCache;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.repository.SkuInfoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Centralizes product-detail cache eviction so SKU-level and SPU-level writes stay consistent.
 *
 * <p>The item cache is keyed by {@code skuId} but the aggregated VO also embeds spuId-bound data
 * (sale attrs, description HTML, spec groups, SPU images). So an SPU-level edit must fan out and
 * evict every SKU under that SPU — otherwise those SKUs serve stale detail until TTL.</p>
 *
 * <p>Eviction runs <b>after commit</b> when a transaction is active, mirroring the existing
 * {@code afterCommit} pattern in the ware service: this prevents a concurrent reader from
 * re-populating the cache with not-yet-committed (or rolled-back) data.</p>
 */
@Component
public class ProductCacheEvictor {

    private final SkuInfoRepository skuInfoRepository;
    private final ProtectedCache protectedCache;

    public ProductCacheEvictor(SkuInfoRepository skuInfoRepository, ProtectedCache protectedCache) {
        this.skuInfoRepository = skuInfoRepository;
        this.protectedCache = protectedCache;
    }

    /** Evicts the cached detail VO for a single SKU. */
    public void evictItemBySkuId(Long skuId) {
        if (skuId == null) {
            return;
        }
        runAfterCommit(() -> protectedCache.evict(ProductCacheKeys.item(skuId)));
    }

    /** Evicts cached detail VOs for every SKU under the given SPU (fan-out for SPU-level edits). */
    public void evictItemsBySpuId(Long spuId) {
        if (spuId == null) {
            return;
        }
        runAfterCommit(() -> {
            List<SkuInfoEntity> skus = skuInfoRepository.findBySpuId(spuId);
            for (SkuInfoEntity sku : skus) {
                if (sku.getSkuId() != null) {
                    protectedCache.evict(ProductCacheKeys.item(sku.getSkuId()));
                }
            }
        });
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
