package com.atguigu.gulimall.coupon.service;

import java.util.List;

public interface CouponProductCleanupService {

    void cleanupBySpuId(Long spuId);

    void cleanupBySkuIds(List<Long> skuIds);
}
