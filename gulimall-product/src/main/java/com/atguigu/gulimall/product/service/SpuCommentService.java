package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuCommentEntity;

import java.util.Collection;
import java.util.Map;

/**
 * 商品评价
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface SpuCommentService {

    SpuCommentEntity getById(Long id);
    void save(SpuCommentEntity entity);
    void updateById(SpuCommentEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);
}

