package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.Collection;
import java.util.Map;

public interface SeckillSessionService {

    PageUtils queryPage(Map<String, Object> params);
    SeckillSessionEntity getById(Long id);
    void save(SeckillSessionEntity entity);
    void updateById(SeckillSessionEntity entity);
    void removeByIds(Collection<?> ids);
}
