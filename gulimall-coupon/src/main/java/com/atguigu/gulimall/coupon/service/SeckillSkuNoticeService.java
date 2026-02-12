package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Collection;
import java.util.Map;

public interface SeckillSkuNoticeService {

    PageUtils queryPage(Map<String, Object> params);
    SeckillSkuNoticeEntity getById(Long id);
    void save(SeckillSkuNoticeEntity entity);
    void updateById(SeckillSkuNoticeEntity entity);
    void removeByIds(Collection<?> ids);
}
