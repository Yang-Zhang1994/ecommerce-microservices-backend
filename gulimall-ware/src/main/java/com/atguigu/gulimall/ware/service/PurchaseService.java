package com.atguigu.gulimall.ware.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PurchaseService {

    PageUtils queryPage(Map<String, Object> params);

    PurchaseEntity getById(Long id);

    void save(PurchaseEntity entity);

    void updateById(PurchaseEntity entity);

    void removeByIds(Collection<?> ids);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}
