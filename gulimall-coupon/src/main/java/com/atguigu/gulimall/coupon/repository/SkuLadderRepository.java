package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SkuLadderRepository extends JpaRepository<SkuLadderEntity, Long> {

    @Modifying
    @Query("DELETE FROM SkuLadderEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") Collection<Long> skuIds);
}
