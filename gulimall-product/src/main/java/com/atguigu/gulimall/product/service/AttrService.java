package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface AttrService {

    AttrEntity getById(Long id);
    void save(AttrEntity entity);
    void updateById(AttrEntity entity);
    void removeByIds(Collection<?> ids);
    List<AttrEntity> listByIds(Collection<Long> ids);

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);


    /**
     * According to the group id, find all related attributes
     */
    List<AttrEntity> getRelationAttr(Long attrGroupId);

    /**
     * Delete attribute and group association
     */
    void deleteRelation(AttrGroupRelationVo[] vos);

    /**
     * Get attributes that are not related to the current attribute group
     */
    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId);
}

