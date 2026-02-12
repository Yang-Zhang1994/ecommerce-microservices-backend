package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.HomeSubjectSpuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeSubjectSpuRepository extends JpaRepository<HomeSubjectSpuEntity, Long> {
}
