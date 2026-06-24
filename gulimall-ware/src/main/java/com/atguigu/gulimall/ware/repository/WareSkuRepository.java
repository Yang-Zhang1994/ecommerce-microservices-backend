package com.atguigu.gulimall.ware.repository;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WareSkuRepository extends JpaRepository<WareSkuEntity, Long>, JpaSpecificationExecutor<WareSkuEntity> {

    List<WareSkuEntity> findBySkuIdAndWareId(Long skuId, Long wareId);
    List<WareSkuEntity> findBySkuId(Long skuId);

    List<WareSkuEntity> findBySkuIdIn(List<Long> skuIds);

    /**
     * 查询当前 sku 的总可用库存量（所有仓库的 库存 - 锁定库存 之和）
     * 等价 SQL: SELECT SUM(stock - COALESCE(stock_locked, 0)) FROM wms_ware_sku WHERE sku_id = :skuId
     */
    @Query(value = "SELECT COALESCE(SUM(stock - COALESCE(stock_locked, 0)), 0) FROM wms_ware_sku WHERE sku_id = :skuId", nativeQuery = true)
    Long getSkuStock(@Param("skuId") Long skuId);

    @Modifying
    @Query(value = """
            UPDATE wms_ware_sku
            SET stock_locked = COALESCE(stock_locked, 0) + :num
            WHERE sku_id = :skuId
              AND ware_id = :wareId
              AND (COALESCE(stock, 0) - COALESCE(stock_locked, 0)) >= :num
            """, nativeQuery = true)
    int lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    @Modifying
    @Query(value = """
            UPDATE wms_ware_sku
            SET stock_locked = GREATEST(COALESCE(stock_locked, 0) - :num, 0)
            WHERE sku_id = :skuId
              AND ware_id = :wareId
            """, nativeQuery = true)
    int unlockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    @Query(value = """
            SELECT DISTINCT sku_id FROM wms_ware_sku
            WHERE sku_id IN (:skuIds) AND COALESCE(stock_locked, 0) > 0
            """, nativeQuery = true)
    List<Long> findSkuIdsWithLockedStock(@Param("skuIds") List<Long> skuIds);

    @Modifying
    @Query(value = "DELETE FROM wms_ware_sku WHERE sku_id IN (:skuIds)", nativeQuery = true)
    void deleteBySkuIdIn(@Param("skuIds") List<Long> skuIds);
}
