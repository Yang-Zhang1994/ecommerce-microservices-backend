package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;

import java.util.Collection;
import java.util.Map;

public interface CouponSpuCategoryRelationService {

    PageUtils queryPage(Map<String, Object> params);
    CouponSpuCategoryRelationEntity getById(Long id);
    void save(CouponSpuCategoryRelationEntity entity);
    void updateById(CouponSpuCategoryRelationEntity entity);
    void removeByIds(Collection<?> ids);
}
