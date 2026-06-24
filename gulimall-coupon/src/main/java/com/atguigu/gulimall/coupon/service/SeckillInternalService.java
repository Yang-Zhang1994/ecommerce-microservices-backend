package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.to.SeckillWarmupSessionVo;

import java.util.List;

public interface SeckillInternalService {

    /** Enabled sessions overlapping [now, now+days] with their SKU relations. */
    List<SeckillWarmupSessionVo> warmupData(int days);
}
