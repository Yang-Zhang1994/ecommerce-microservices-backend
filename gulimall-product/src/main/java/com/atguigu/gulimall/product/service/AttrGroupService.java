package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface AttrGroupService {

    AttrGroupEntity getById(Long id);
    void save(AttrGroupEntity entity);
    void updateById(AttrGroupEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    /**
     * Get all attribute groups with their attributes for a specific catalog
     */
    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    /**
     * Base attr values for an SPU, joined to attr groups filtered by catalog (same shape as MyBatis getAttrGroupWithAttrsBySpuId).
     */
    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

