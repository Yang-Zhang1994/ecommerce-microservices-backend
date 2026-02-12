package com.atguigu.gulimall.ware.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * HTTP Interface for gulimall-product (replaces ProductFeignService).
 */
@HttpExchange
public interface ProductApi {

    @GetExchange("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
