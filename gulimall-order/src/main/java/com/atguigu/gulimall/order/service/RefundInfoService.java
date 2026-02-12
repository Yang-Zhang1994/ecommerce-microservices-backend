package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.RefundInfoEntity;

import java.util.Collection;
import java.util.Map;

public interface RefundInfoService {

    PageUtils queryPage(Map<String, Object> params);
    RefundInfoEntity getById(Long id);
    void save(RefundInfoEntity entity);
    void updateById(RefundInfoEntity entity);
    void removeByIds(Collection<?> ids);
}
