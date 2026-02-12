package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface CategoryBrandRelationService {

    CategoryBrandRelationEntity getById(Long id);
    void save(CategoryBrandRelationEntity entity);
    void updateById(CategoryBrandRelationEntity entity);
    void removeByIds(Collection<?> ids);
    List<CategoryBrandRelationEntity> listByBrandId(Long brandId);

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);


    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    /**
     * Get all brands related to a category
     * @param catId category id
     * @return list of BrandEntity
     */
    List<BrandEntity> getBrandById(Long catId);
}

