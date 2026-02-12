package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CommentReplayEntity;

import java.util.Collection;
import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface CommentReplayService {

    CommentReplayEntity getById(Long id);
    void save(CommentReplayEntity entity);
    void updateById(CommentReplayEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);
}

