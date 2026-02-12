package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderItemEntity;

import java.util.Collection;
import java.util.Map;

public interface OrderItemService {

    PageUtils queryPage(Map<String, Object> params);
    OrderItemEntity getById(Long id);
    void save(OrderItemEntity entity);
    void updateById(OrderItemEntity entity);
    void removeByIds(Collection<?> ids);
}
