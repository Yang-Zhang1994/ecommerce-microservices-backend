package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.ware.SkuHasStockVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.OrderLockStockResultVo;
import com.atguigu.gulimall.ware.vo.OrderWareLockVo;
import com.atguigu.gulimall.ware.vo.WareStockUnlockVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 */
public interface WareSkuService {

    PageUtils queryPage(Map<String, Object> params);

    WareSkuEntity getById(Long id);

    void save(WareSkuEntity entity);

    void updateById(WareSkuEntity entity);

    void removeByIds(Collection<?> ids);

    /**
     * add stock
     */
    void addStock(Long skuId, Long wareId, Integer skuNum);

    /**
     * 批量查询 sku 是否有库存（可用库存 = 总库存 - 锁定库存 > 0），返回 VO 列表
     */
    List<SkuHasStockVo> hasStockBySkuIds(List<Long> skuIds);

    /**
     * Lock stock, persist work-order task + details, schedule delayed release message.
     */
    OrderLockStockResultVo lockOrderStock(OrderWareLockVo lockVo);

    /**
     * Unlock: prefer {@link WareStockUnlockVo#getTaskId()} (updates wms_ware_sku + detail lock_status);
     * otherwise legacy unlock by {@link WareStockUnlockVo#getLocked()} only.
     */
    void unlockOrderStock(WareStockUnlockVo vo);

    /**
     * Unlock all lines still in LOCKED status for this task (idempotent).
     */
    void unlockByTaskId(Long taskId);

    /**
     * Refresh sku_name from product catalog and default null stock_locked to 0.
     */
    Map<String, Object> syncFromProduct();

    List<Long> findSkuIdsWithLockedStock(List<Long> skuIds);

    void deleteBySkuIds(List<Long> skuIds);
}
