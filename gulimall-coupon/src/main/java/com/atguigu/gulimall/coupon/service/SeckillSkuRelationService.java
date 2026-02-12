package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;

import java.util.Collection;
import java.util.Map;

public interface SeckillSkuRelationService {

    PageUtils queryPage(Map<String, Object> params);
    SeckillSkuRelationEntity getById(Long id);
    void save(SeckillSkuRelationEntity entity);
    void updateById(SeckillSkuRelationEntity entity);
    void removeByIds(Collection<?> ids);
}
