package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Collection;
import java.util.Map;

public interface HomeSubjectSpuService {

    PageUtils queryPage(Map<String, Object> params);
    HomeSubjectSpuEntity getById(Long id);
    void save(HomeSubjectSpuEntity entity);
    void updateById(HomeSubjectSpuEntity entity);
    void removeByIds(Collection<?> ids);
}
