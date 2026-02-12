package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillPromotionEntity;

import java.util.Collection;
import java.util.Map;

public interface SeckillPromotionService {

    PageUtils queryPage(Map<String, Object> params);
    SeckillPromotionEntity getById(Long id);
    void save(SeckillPromotionEntity entity);
    void updateById(SeckillPromotionEntity entity);
    void removeByIds(Collection<?> ids);
}
