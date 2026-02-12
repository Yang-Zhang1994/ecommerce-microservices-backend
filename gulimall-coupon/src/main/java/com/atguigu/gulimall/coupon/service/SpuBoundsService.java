package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SpuBoundsEntity;

import java.util.Collection;
import java.util.Map;

public interface SpuBoundsService {

    PageUtils queryPage(Map<String, Object> params);
    SpuBoundsEntity getById(Long id);
    void save(SpuBoundsEntity entity);
    void updateById(SpuBoundsEntity entity);
    void removeByIds(Collection<?> ids);
}
