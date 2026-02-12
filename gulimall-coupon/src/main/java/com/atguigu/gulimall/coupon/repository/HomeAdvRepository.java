package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.HomeAdvEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeAdvRepository extends JpaRepository<HomeAdvEntity, Long> {
}
