package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;

import java.util.Collection;
import java.util.Map;

public interface SkuLadderService {

    PageUtils queryPage(Map<String, Object> params);
    SkuLadderEntity getById(Long id);
    void save(SkuLadderEntity entity);
    void updateById(SkuLadderEntity entity);
    void removeByIds(Collection<?> ids);
}
