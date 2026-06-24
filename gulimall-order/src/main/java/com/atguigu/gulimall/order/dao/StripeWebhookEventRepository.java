package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.StripeWebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEventEntity, Long> {

    Optional<StripeWebhookEventEntity> findByEventId(String eventId);
}
