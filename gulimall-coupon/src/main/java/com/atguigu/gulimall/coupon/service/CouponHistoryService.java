package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.CouponHistoryEntity;

import java.util.Collection;
import java.util.Map;

public interface CouponHistoryService {

    PageUtils queryPage(Map<String, Object> params);
    CouponHistoryEntity getById(Long id);
    void save(CouponHistoryEntity entity);
    void updateById(CouponHistoryEntity entity);
    void removeByIds(Collection<?> ids);
}
