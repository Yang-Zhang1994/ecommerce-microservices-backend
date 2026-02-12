package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SpuSaveVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Collection;
import java.util.Map;

/**
 * spu信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
public interface SpuInfoService {

    SpuInfoEntity getById(Long id);
    void save(SpuInfoEntity entity);
    void updateById(SpuInfoEntity entity);
    void removeByIds(Collection<?> ids);

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    /**
     * Query spu info by condition
     */
    PageUtils queryPageByCondition(Map<String, Object> params);
}

