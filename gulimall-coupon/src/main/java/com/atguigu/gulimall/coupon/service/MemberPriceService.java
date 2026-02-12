package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberPriceService {

    PageUtils queryPage(Map<String, Object> params);
    MemberPriceEntity getById(Long id);
    void save(MemberPriceEntity entity);
    void updateById(MemberPriceEntity entity);
    void removeByIds(Collection<?> ids);
}
