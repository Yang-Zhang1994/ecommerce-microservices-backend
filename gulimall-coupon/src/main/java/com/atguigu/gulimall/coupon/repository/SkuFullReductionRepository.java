package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SkuFullReductionRepository extends JpaRepository<SkuFullReductionEntity, Long> {

    @Modifying
    @Query("DELETE FROM SkuFullReductionEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") Collection<Long> skuIds);
}
