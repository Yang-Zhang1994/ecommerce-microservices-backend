package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.HomeSubjectEntity;

import java.util.Collection;
import java.util.Map;

public interface HomeSubjectService {

    PageUtils queryPage(Map<String, Object> params);
    HomeSubjectEntity getById(Long id);
    void save(HomeSubjectEntity entity);
    void updateById(HomeSubjectEntity entity);
    void removeByIds(Collection<?> ids);
}
