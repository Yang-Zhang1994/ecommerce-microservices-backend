package com.atguigu.common.client;

import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * HTTP Interface for gulimall-order (internal product-delete guards).
 */
@HttpExchange
public interface OrderApi {

    /**
     * SKU ids that appear on orders not in a final state (pending / paid / shipped).
     */
    @PostExchange("/order/internal/sku/active-order-skus")
    R activeOrderSkus(@RequestBody List<Long> skuIds);
}
