package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SeckillSkuNoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SeckillSkuNoticeRepository extends JpaRepository<SeckillSkuNoticeEntity, Long> {

    @Modifying
    @Query("DELETE FROM SeckillSkuNoticeEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") Collection<Long> skuIds);
}
