package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.HomeAdvEntity;

import java.util.Collection;
import java.util.Map;

public interface HomeAdvService {

    PageUtils queryPage(Map<String, Object> params);
    HomeAdvEntity getById(Long id);
    void save(HomeAdvEntity entity);
    void updateById(HomeAdvEntity entity);
    void removeByIds(Collection<?> ids);
}
