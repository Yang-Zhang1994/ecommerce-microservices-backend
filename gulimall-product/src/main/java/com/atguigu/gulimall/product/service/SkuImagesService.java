package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;

import java.util.Collection;
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
}

