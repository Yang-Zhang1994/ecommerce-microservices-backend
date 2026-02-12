package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface AttrAttrgroupRelationService {

    AttrAttrgroupRelationEntity getById(Long id);
    void save(AttrAttrgroupRelationEntity entity);
    void updateById(AttrAttrgroupRelationEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    /**
     * Save attribute and group associations in bulk
     */
    void saveRelations(List<AttrGroupRelationVo> vos);
}

