package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfoEntity, Long> {
    Optional<PaymentInfoEntity> findByOrderSn(String orderSn);
}
