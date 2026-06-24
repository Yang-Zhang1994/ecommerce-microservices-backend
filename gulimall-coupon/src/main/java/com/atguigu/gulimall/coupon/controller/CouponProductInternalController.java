package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.coupon.service.CouponProductCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coupon/internal/product")
public class CouponProductInternalController {

    @Autowired
    private CouponProductCleanupService couponProductCleanupService;

    @PostMapping("/cleanup-spu")
    public R cleanupSpu(@RequestBody Long spuId) {
        couponProductCleanupService.cleanupBySpuId(spuId);
        return R.ok();
    }

    @PostMapping("/cleanup-skus")
    public R cleanupSkus(@RequestBody List<Long> skuIds) {
        couponProductCleanupService.cleanupBySkuIds(skuIds);
        return R.ok();
    }
}
