package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderSettingEntity;

import java.util.Collection;
import java.util.Map;

public interface OrderSettingService {

    PageUtils queryPage(Map<String, Object> params);
    OrderSettingEntity getById(Long id);
    void save(OrderSettingEntity entity);
    void updateById(OrderSettingEntity entity);
    void removeByIds(Collection<?> ids);
}
