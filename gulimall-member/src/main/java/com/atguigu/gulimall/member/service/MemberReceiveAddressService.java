package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberReceiveAddressService {

    PageUtils queryPage(Map<String, Object> params);
    MemberReceiveAddressEntity getById(Long id);
    void save(MemberReceiveAddressEntity entity);
    void updateById(MemberReceiveAddressEntity entity);
    void removeByIds(Collection<?> ids);
}
