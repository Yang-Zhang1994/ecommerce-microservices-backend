package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface SkuImagesService {

    SkuImagesEntity getById(Long id);
    void save(SkuImagesEntity entity);
    void updateById(SkuImagesEntity entity);
    void removeByIds(Collection<?> ids);
    void saveAll(Iterable<SkuImagesEntity> entities);

    PageUtils queryPage(Map<String, Object> params);

    /**
     * List all images for a given skuId.
     */
    List<SkuImagesEntity> listBySkuId(Long skuId);

    /**
     * Replace all images of a SKU with the given list.
     */
    void saveBatchForSku(Long skuId, List<SkuImagesEntity> images);
}

