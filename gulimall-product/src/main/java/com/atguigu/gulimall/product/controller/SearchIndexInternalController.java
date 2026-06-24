package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.service.ProductSearchIndexSyncService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * Internal APIs for other services (e.g. ware) to refresh Elasticsearch after stock changes.
 * Failures are reported per call; callers should not roll back warehouse transactions.
 */
@RestController
@RequestMapping("/product/internal/search-index")
public class SearchIndexInternalController {

    private final ProductSearchIndexSyncService productSearchIndexSyncService;

    public SearchIndexInternalController(ProductSearchIndexSyncService productSearchIndexSyncService) {
        this.productSearchIndexSyncService = productSearchIndexSyncService;
    }

    @PostMapping("/refresh-by-sku/{skuId}")
    public R refreshBySkuId(@PathVariable("skuId") Long skuId) {
        boolean synced = productSearchIndexSyncService.refreshIfOnSaleBySkuId(skuId);
        return R.ok().put("searchSynced", synced).put("skuId", skuId);
    }

    @PostMapping("/refresh-by-skus")
    public R refreshBySkuIds(@RequestBody List<Long> skuIds) {
        int synced = productSearchIndexSyncService.refreshBySkuIds(skuIds);
        return R.ok().put("searchSyncedCount", synced).put("requested", skuIds == null ? 0 : skuIds.size());
    }
}
