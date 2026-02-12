package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberCollectSubjectEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberCollectSubjectService {

    PageUtils queryPage(Map<String, Object> params);
    MemberCollectSubjectEntity getById(Long id);
    void save(MemberCollectSubjectEntity entity);
    void updateById(MemberCollectSubjectEntity entity);
    void removeByIds(Collection<?> ids);
}
