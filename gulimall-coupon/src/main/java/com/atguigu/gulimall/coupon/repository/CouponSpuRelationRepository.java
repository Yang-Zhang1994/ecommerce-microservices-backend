package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.CouponSpuRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponSpuRelationRepository extends JpaRepository<CouponSpuRelationEntity, Long> {

    @Modifying
    @Query("DELETE FROM CouponSpuRelationEntity e WHERE e.spuId = :spuId")
    void deleteBySpuId(@Param("spuId") Long spuId);
}
