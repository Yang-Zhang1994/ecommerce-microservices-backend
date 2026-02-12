package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeckillSessionRepository extends JpaRepository<SeckillSessionEntity, Long> {
}
