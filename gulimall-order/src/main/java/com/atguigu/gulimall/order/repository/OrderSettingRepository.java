package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.OrderSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSettingRepository extends JpaRepository<OrderSettingEntity, Long> {
}
