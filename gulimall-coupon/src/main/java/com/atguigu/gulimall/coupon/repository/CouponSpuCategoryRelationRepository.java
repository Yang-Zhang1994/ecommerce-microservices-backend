package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponSpuCategoryRelationRepository extends JpaRepository<CouponSpuCategoryRelationEntity, Long> {
}
