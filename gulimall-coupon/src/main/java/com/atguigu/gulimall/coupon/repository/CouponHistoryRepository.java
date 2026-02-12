package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.CouponHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponHistoryRepository extends JpaRepository<CouponHistoryEntity, Long> {
}
