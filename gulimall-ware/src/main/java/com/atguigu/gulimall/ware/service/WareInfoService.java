package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.util.Collection;
import java.util.Map;

public interface WareInfoService {

    PageUtils queryPage(Map<String, Object> params);

    WareInfoEntity getById(Long id);

    void save(WareInfoEntity entity);

    void updateById(WareInfoEntity entity);

    void removeByIds(Collection<?> ids);
}
