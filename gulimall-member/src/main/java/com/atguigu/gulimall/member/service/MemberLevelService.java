package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberLevelService {

    PageUtils queryPage(Map<String, Object> params);
    MemberLevelEntity getById(Long id);
    void save(MemberLevelEntity entity);
    void updateById(MemberLevelEntity entity);
    void removeByIds(Collection<?> ids);
}
