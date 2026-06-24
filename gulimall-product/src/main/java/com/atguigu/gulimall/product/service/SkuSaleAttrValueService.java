package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface SkuSaleAttrValueService {

    SkuSaleAttrValueEntity getById(Long id);
    void save(SkuSaleAttrValueEntity entity);
    void updateById(SkuSaleAttrValueEntity entity);
    void removeByIds(Collection<?> ids);
    void saveAll(Iterable<SkuSaleAttrValueEntity> entities);

    PageUtils queryPage(Map<String, Object> params);

    /**
     * Sale attributes for an SPU: merge rows from all SKUs under that SPU (same idea as getSaleAttrBySpuId in MyBatis samples).
     */
    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

    /**
     * Flatten sale attrs for one SKU into strings like "Color:Black".
     */
    List<String> getSaleAttrStringsBySkuId(Long skuId);

    /** All sale attr rows for the given SKU ids (ES indexing). */
    List<SkuSaleAttrValueEntity> listBySkuIds(Collection<Long> skuIds);
}

