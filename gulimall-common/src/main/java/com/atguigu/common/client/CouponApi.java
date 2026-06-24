package com.atguigu.common.client;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * HTTP Interface for gulimall-coupon (replaces CouponFeignService).
 */
@HttpExchange
public interface CouponApi {

    @PostExchange("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostExchange("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);

    @GetExchange("/coupon/coupon/member/list")
    R memberCoupons();

    @PostExchange("/coupon/internal/product/cleanup-spu")
    R cleanupSpu(@RequestBody Long spuId);

    @PostExchange("/coupon/internal/product/cleanup-skus")
    R cleanupSkus(@RequestBody List<Long> skuIds);

    /** Upcoming enabled seckill sessions + SKU relations for gulimall-seckill warm-up. */
    @GetExchange("/coupon/internal/seckill/warmup")
    R seckillWarmup(@RequestParam("days") int days);
}
