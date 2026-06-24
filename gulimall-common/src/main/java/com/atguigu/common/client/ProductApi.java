package com.atguigu.common.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * HTTP client for gulimall-product.
 */
@HttpExchange
public interface ProductApi {

    @GetExchange("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @GetExchange("/product/skusaleattrvalue/strings/{skuId}")
    R saleAttrStrings(@PathVariable("skuId") Long skuId);
}
