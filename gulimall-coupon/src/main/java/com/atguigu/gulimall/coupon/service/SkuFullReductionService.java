package com.atguigu.gulimall.coupon.service;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Collection;
import java.util.Map;

public interface SkuFullReductionService {

    PageUtils queryPage(Map<String, Object> params);
    SkuFullReductionEntity getById(Long id);
    void save(SkuFullReductionEntity entity);
    void updateById(SkuFullReductionEntity entity);
    void removeByIds(Collection<?> ids);

    /** Save sku reduction info (ladder + full reduction + member price) from product service */
    void saveSkuReduction(SkuReductionTo skuReductionTo);
}
