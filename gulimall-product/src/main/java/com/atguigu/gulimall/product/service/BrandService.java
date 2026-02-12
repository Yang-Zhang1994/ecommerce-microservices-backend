package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.BrandEntity;

import java.util.Collection;
import java.util.Map;

/**
 * 品牌
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface BrandService {

    BrandEntity getById(Long id);
    void save(BrandEntity entity);
    void updateById(BrandEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

