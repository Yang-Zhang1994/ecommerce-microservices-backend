package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Collection;
import java.util.Map;

public interface IntegrationChangeHistoryService {

    PageUtils queryPage(Map<String, Object> params);
    IntegrationChangeHistoryEntity getById(Long id);
    void save(IntegrationChangeHistoryEntity entity);
    void updateById(IntegrationChangeHistoryEntity entity);
    void removeByIds(Collection<?> ids);
}
