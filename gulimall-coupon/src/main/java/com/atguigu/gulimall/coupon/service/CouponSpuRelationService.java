package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.CouponSpuRelationEntity;

import java.util.Collection;
import java.util.Map;

public interface CouponSpuRelationService {

    PageUtils queryPage(Map<String, Object> params);
    CouponSpuRelationEntity getById(Long id);
    void save(CouponSpuRelationEntity entity);
    void updateById(CouponSpuRelationEntity entity);
    void removeByIds(Collection<?> ids);
}
