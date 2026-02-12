package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface CategoryService {

    CategoryEntity getById(Long id);
    void save(CategoryEntity entity);
    void updateById(CategoryEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenusByIds(List<Long> list);

    /**
     * Find the complete path of catelogId
     * @param catelogId
     * @return [ParentId, ..., catelogId]
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    void updateBatchById(java.util.List<CategoryEntity> list);
}

