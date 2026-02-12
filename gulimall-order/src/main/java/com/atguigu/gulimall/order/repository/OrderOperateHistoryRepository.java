package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.OrderOperateHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderOperateHistoryRepository extends JpaRepository<OrderOperateHistoryEntity, Long> {
}
