package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.CouponSpuRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponSpuRelationRepository extends JpaRepository<CouponSpuRelationEntity, Long> {
}
