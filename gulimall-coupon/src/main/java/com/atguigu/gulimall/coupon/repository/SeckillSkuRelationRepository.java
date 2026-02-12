package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeckillSkuRelationRepository extends JpaRepository<SeckillSkuRelationEntity, Long> {
}
