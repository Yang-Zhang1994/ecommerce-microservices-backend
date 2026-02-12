package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface ProductAttrValueService {

    ProductAttrValueEntity getById(Long id);
    void save(ProductAttrValueEntity entity);
    void updateById(ProductAttrValueEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> list);

    /**
     * base attr list for spu
     */
    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    /**
     * update spu attr
     */
    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}

