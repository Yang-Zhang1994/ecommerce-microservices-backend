package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SkuItemVo;

/**
 * Aggregated data for the product detail page.
 */
public interface SkuItemService {

    /**
     * Builds all detail blocks for the given skuId; returns null if the SKU does not exist.
     */
    SkuItemVo item(Long skuId);
}
