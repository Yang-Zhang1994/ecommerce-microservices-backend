package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SpuBoundsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuBoundsRepository extends JpaRepository<SpuBoundsEntity, Long> {

    @Modifying
    @Query("DELETE FROM SpuBoundsEntity e WHERE e.spuId = :spuId")
    void deleteBySpuId(@Param("spuId") Long spuId);
}
