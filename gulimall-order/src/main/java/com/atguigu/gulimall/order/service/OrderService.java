package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Collection;
import java.util.Map;

public interface OrderService {

    PageUtils queryPage(Map<String, Object> params);
    OrderEntity getById(Long id);
    void save(OrderEntity entity);
    void updateById(OrderEntity entity);
    void removeByIds(Collection<?> ids);
}
