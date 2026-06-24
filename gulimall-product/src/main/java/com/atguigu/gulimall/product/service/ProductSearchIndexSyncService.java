package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.R;

import java.util.Collection;

/**
 * 商品已上架时，将当前 MySQL 数据刷新到 ES 搜索索引。
 */
public interface ProductSearchIndexSyncService {

    /**
     * @return true 表示 SPU 已上架且已向 ES 写入 SKU 文档
     */
    boolean refreshIfOnSale(Long spuId);

    /**
     * 根据 SKU 解析 SPU 后刷新搜索索引。
     */
    boolean refreshIfOnSaleBySkuId(Long skuId);

    /**
     * 批量按 SKU 刷新（仅已上架 SPU 会写入 ES）。返回成功刷新的 SKU 数量。
     */
    int refreshBySkuIds(Collection<Long> skuIds);

    static R okWithSearchSync(boolean searchSynced) {
        return R.ok().put("searchSynced", searchSynced);
    }
}
