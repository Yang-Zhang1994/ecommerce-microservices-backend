package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberLoginLogEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberLoginLogService {

    PageUtils queryPage(Map<String, Object> params);
    MemberLoginLogEntity getById(Long id);
    void save(MemberLoginLogEntity entity);
    void updateById(MemberLoginLogEntity entity);
    void removeByIds(Collection<?> ids);
}
