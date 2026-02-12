package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuLadderRepository extends JpaRepository<SkuLadderEntity, Long> {
}
