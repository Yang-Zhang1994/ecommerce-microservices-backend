package com.atguigu.gulimall.ware.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * HTTP Interface for gulimall-product (replaces ProductFeignService).
 */
@HttpExchange
public interface ProductApi {

    @GetExchange("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    /** Refresh ES product index for an on-sale SPU resolved from skuId (after stock change). */
    @PostExchange("/product/internal/search-index/refresh-by-sku/{skuId}")
    R refreshSearchIndexBySkuId(@PathVariable("skuId") Long skuId);

    @PostExchange("/product/internal/search-index/refresh-by-skus")
    R refreshSearchIndexBySkuIds(@RequestBody List<Long> skuIds);
}
