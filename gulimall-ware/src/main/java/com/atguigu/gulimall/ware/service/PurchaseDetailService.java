package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PurchaseDetailService {

    PageUtils queryPage(Map<String, Object> params);

    PurchaseDetailEntity getById(Long id);

    void save(PurchaseDetailEntity entity);

    void updateById(PurchaseDetailEntity entity);

    void removeByIds(Collection<?> ids);

    List<PurchaseDetailEntity> listByIds(Collection<Long> ids);

    void updateBatchById(List<PurchaseDetailEntity> list);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long purchaseId);
}
