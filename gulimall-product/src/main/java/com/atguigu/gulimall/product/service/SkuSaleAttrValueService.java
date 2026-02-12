package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;

import java.util.Collection;
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
}

