package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuFullReductionRepository extends JpaRepository<SkuFullReductionEntity, Long> {
}
