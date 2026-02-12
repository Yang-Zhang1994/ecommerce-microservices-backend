package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.GrowthChangeHistoryEntity;

import java.util.Collection;
import java.util.Map;

public interface GrowthChangeHistoryService {

    PageUtils queryPage(Map<String, Object> params);
    GrowthChangeHistoryEntity getById(Long id);
    void save(GrowthChangeHistoryEntity entity);
    void updateById(GrowthChangeHistoryEntity entity);
    void removeByIds(Collection<?> ids);
}
