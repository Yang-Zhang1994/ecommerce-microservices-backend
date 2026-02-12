package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.Collection;
import java.util.Map;

/**
 * 商品库存
 */
public interface WareSkuService {

    PageUtils queryPage(Map<String, Object> params);

    WareSkuEntity getById(Long id);

    void save(WareSkuEntity entity);

    void updateById(WareSkuEntity entity);

    void removeByIds(Collection<?> ids);

    /**
     * add stock
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);
}
