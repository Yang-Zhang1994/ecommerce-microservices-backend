package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;

import java.util.Collection;
import java.util.Map;

public interface WareOrderTaskService {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getById(Long id);

    void save(WareOrderTaskEntity entity);

    void updateById(WareOrderTaskEntity entity);

    void removeByIds(Collection<?> ids);
}
