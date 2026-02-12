package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Collection;
import java.util.Map;

public interface WareOrderTaskDetailService {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskDetailEntity getById(Long id);

    void save(WareOrderTaskDetailEntity entity);

    void updateById(WareOrderTaskDetailEntity entity);

    void removeByIds(Collection<?> ids);
}
