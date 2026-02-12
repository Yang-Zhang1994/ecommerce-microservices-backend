package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderOperateHistoryEntity;

import java.util.Collection;
import java.util.Map;

public interface OrderOperateHistoryService {

    PageUtils queryPage(Map<String, Object> params);
    OrderOperateHistoryEntity getById(Long id);
    void save(OrderOperateHistoryEntity entity);
    void updateById(OrderOperateHistoryEntity entity);
    void removeByIds(Collection<?> ids);
}
