package com.atguigu.common.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * HTTP Interface for gulimall-ware（库存服务）.
 */
@HttpExchange
public interface WareApi {

    /**
     * 批量查询 sku 是否有库存，返回 R.data = Map&lt;skuId, hasStock&gt;
     */
    @PostExchange("/ware/waresku/hasStock")
    R hasStock(@RequestBody List<Long> skuIds);

    /** SKU ids with locked stock (stock_locked > 0). */
    @PostExchange("/ware/waresku/locked-skus")
    R lockedSkus(@RequestBody List<Long> skuIds);

    /** Remove all warehouse rows for the given SKU ids. */
    @PostExchange("/ware/waresku/delete-by-sku-ids")
    R deleteBySkuIds(@RequestBody List<Long> skuIds);
}
