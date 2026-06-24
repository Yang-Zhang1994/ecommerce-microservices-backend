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

    /**
     * 商品上架。返回 true 表示 ES 写入成功且已更新 DB 上架状态。
     */
    boolean up(Long spuId);

    /**
     * 商品下架。从 ES 删除该 SPU 下所有 SKU，并更新 DB 为下架状态。
     */
    boolean down(Long spuId);

    /**
     * 若 SPU 已上架，用当前 SKU 数据（含图片）刷新 ES 索引，不改变上架状态。
     *
     * @return true 表示已上架且成功写入 ES；未上架或无 SKU 时返回 false
     */
    boolean refreshSearchIndexIfOnSale(Long spuId);
}

