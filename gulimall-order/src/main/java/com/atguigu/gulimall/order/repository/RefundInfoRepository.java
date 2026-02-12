package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.RefundInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundInfoRepository extends JpaRepository<RefundInfoEntity, Long> {
}
