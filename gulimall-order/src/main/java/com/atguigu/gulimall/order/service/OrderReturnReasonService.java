package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;

import java.util.Collection;
import java.util.Map;

public interface OrderReturnReasonService {

    PageUtils queryPage(Map<String, Object> params);
    OrderReturnReasonEntity getById(Long id);
    void save(OrderReturnReasonEntity entity);
    void updateById(OrderReturnReasonEntity entity);
    void removeByIds(Collection<?> ids);
}
