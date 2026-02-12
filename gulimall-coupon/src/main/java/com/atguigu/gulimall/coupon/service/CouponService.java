package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.CouponEntity;

import java.util.Collection;
import java.util.Map;

public interface CouponService {

    PageUtils queryPage(Map<String, Object> params);

    CouponEntity getById(Long id);

    void save(CouponEntity entity);

    void updateById(CouponEntity entity);

    void removeByIds(Collection<?> ids);
}
