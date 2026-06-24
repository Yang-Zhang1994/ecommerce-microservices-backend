package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderSn(String orderSn);

    Optional<OrderEntity> findByOrderSnAndMemberId(String orderSn, Long memberId);

    Page<OrderEntity> findByMemberIdOrderByCreateTimeDesc(Long memberId, Pageable pageable);

    Page<OrderEntity> findByMemberIdAndStatusInOrderByCreateTimeDesc(Long memberId, Collection<Integer> statuses, Pageable pageable);
}
