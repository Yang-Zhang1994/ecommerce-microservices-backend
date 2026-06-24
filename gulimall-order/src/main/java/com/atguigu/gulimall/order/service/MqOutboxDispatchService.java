package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.config.OrderRabbitMqConfig;
import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.atguigu.gulimall.order.repository.MqMessageRepository;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Claim (committed in {@link MqOutboxClaimService}) then publish with {@link CorrelationData#getId()} = message_id.
 * Final {@code SENT} is set in {@link MqOutboxBrokerAckService} when broker confirms (requires correlated confirms).
 */
@Service
public class MqOutboxDispatchService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MqMessageRepository mqMessageRepository;
    @Autowired
    private MqOutboxClaimService mqOutboxClaimService;

    @Value("${order.flash-pay-timeout-minutes:15}")
    private int flashPayTimeoutMinutes;

    public void sendAndMarkSent(String messageId) {
        if (!mqOutboxClaimService.claimNewToPending(messageId)) {
            return;
        }
        MqMessageEntity row = mqMessageRepository.findById(messageId).orElse(null);
        if (row == null) {
            mqOutboxClaimService.revertPendingToNew(messageId);
            return;
        }
        try {
            MessagePostProcessor ttl = seckillDelayPostProcessor(row.getRoutingKey());
            if (ttl != null) {
                rabbitTemplate.convertAndSend(
                        row.getToExchange(),
                        row.getRoutingKey(),
                        row.getContent(),
                        ttl,
                        new CorrelationData(messageId)
                );
            } else {
                rabbitTemplate.convertAndSend(
                        row.getToExchange(),
                        row.getRoutingKey(),
                        row.getContent(),
                        new CorrelationData(messageId)
                );
            }
        } catch (Exception e) {
            mqOutboxClaimService.revertPendingToNew(messageId);
            throw e;
        }
    }

    private MessagePostProcessor seckillDelayPostProcessor(String routingKey) {
        if (!OrderRabbitMqConfig.ROUTING_KEY_ORDER_SECKILL_WAIT_PAY.equals(routingKey)) {
            return null;
        }
        String expiration = String.valueOf(Math.max(1, flashPayTimeoutMinutes) * 60L * 1000L);
        return message -> {
            message.getMessageProperties().setExpiration(expiration);
            return message;
        };
    }
}
