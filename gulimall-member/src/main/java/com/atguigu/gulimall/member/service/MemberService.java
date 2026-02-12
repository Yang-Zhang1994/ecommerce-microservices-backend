package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberService {

    PageUtils queryPage(Map<String, Object> params);
    MemberEntity getById(Long id);
    void save(MemberEntity entity);
    void updateById(MemberEntity entity);
    void removeByIds(Collection<?> ids);
}
