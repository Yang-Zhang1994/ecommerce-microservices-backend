package com.atguigu.gulimall.search.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * HTTP interface for brand APIs in gulimall-product.
 */
@HttpExchange
public interface BrandApi {

    /**
     * Batch query brand infos by ids.
     * Maps to /product/brand/infos?brandIds=1&brandIds=2
     */
    @GetExchange("/product/brand/infos")
    R infos(@RequestParam("brandIds") List<Long> brandIds);
}

