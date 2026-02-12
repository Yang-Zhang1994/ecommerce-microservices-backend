package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberCollectSpuEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberCollectSpuService {

    PageUtils queryPage(Map<String, Object> params);
    MemberCollectSpuEntity getById(Long id);
    void save(MemberCollectSpuEntity entity);
    void updateById(MemberCollectSpuEntity entity);
    void removeByIds(Collection<?> ids);
}
