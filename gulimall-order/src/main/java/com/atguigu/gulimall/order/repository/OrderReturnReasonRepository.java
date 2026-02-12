package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReturnReasonRepository extends JpaRepository<OrderReturnReasonEntity, Long> {
}
