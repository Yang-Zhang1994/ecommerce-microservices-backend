package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SpuBoundsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuBoundsRepository extends JpaRepository<SpuBoundsEntity, Long> {
}
