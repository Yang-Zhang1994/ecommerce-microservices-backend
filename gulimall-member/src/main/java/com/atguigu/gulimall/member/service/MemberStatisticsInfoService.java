package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberStatisticsInfoEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberStatisticsInfoService {

    PageUtils queryPage(Map<String, Object> params);
    MemberStatisticsInfoEntity getById(Long id);
    void save(MemberStatisticsInfoEntity entity);
    void updateById(MemberStatisticsInfoEntity entity);
    void removeByIds(Collection<?> ids);
}
