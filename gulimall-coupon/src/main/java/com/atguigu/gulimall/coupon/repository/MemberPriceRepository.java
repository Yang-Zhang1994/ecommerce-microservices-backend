package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPriceRepository extends JpaRepository<MemberPriceEntity, Long> {
}
