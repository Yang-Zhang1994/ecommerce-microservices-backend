package com.atguigu.common.client;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.GetExchange;

/**
 * HTTP Interface for gulimall-coupon (replaces CouponFeignService).
 */
@HttpExchange
public interface CouponApi {

    @PostExchange("/coupon/spubounds/save")
    R saveSpuBounds(SpuBoundTo spuBoundTo);

    @PostExchange("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(SkuReductionTo skuReductionTo);

    @GetExchange("/coupon/coupon/member/list")
    R memberCoupons();
}
