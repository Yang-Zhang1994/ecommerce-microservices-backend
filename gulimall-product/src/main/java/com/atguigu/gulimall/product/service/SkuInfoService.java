package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.vo.OrderSkuMetaVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface SkuInfoService {

    SkuInfoEntity getById(Long id);
    void save(SkuInfoEntity entity);
    void updateById(SkuInfoEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    /**
     * Query paginated information based on conditions
     */
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据 spuId 查询所有 sku
     */
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /**
     * Batch order-item meta by skuIds (spu/category/brand fields).
     */
    List<OrderSkuMetaVo> getOrderSkuMetaBySkuIds(List<Long> skuIds);
}

