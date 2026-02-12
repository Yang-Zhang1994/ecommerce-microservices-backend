package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Images;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuImagesEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * spu图片
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface SpuImagesService {

    SpuImagesEntity getById(Long id);
    void save(SpuImagesEntity entity);
    void updateById(SpuImagesEntity entity);
    void removeByIds(Collection<?> ids);
    void saveAll(Iterable<SpuImagesEntity> entities);

    PageUtils queryPage(Map<String, Object> params);

    void saveImages(Long id, List<Images> images);
}

