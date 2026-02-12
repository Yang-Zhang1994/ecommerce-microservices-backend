package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.HomeSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeSubjectRepository extends JpaRepository<HomeSubjectEntity, Long> {
}
