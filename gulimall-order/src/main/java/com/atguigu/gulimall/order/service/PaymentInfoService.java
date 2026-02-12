package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;

import java.util.Collection;
import java.util.Map;

public interface PaymentInfoService {

    PageUtils queryPage(Map<String, Object> params);
    PaymentInfoEntity getById(Long id);
    void save(PaymentInfoEntity entity);
    void updateById(PaymentInfoEntity entity);
    void removeByIds(Collection<?> ids);
}
