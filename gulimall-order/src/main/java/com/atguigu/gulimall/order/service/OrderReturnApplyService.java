package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;

import java.util.Collection;
import java.util.Map;

public interface OrderReturnApplyService {

    PageUtils queryPage(Map<String, Object> params);
    OrderReturnApplyEntity getById(Long id);
    void save(OrderReturnApplyEntity entity);
    void updateById(OrderReturnApplyEntity entity);
    void removeByIds(Collection<?> ids);
}
