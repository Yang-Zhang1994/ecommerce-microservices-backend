package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.service.MqOutboxBrokerAckService;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Requires {@code spring.rabbitmq.publisher-confirm-type=correlated} and optional returns.
 */
@Configuration
public class OrderRabbitPublisherConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MqOutboxBrokerAckService mqOutboxBrokerAckService;

    @PostConstruct
    public void initPublisherCallbacks() {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(mqOutboxBrokerAckService::onPublisherConfirm);
        rabbitTemplate.setReturnsCallback(mqOutboxBrokerAckService::onPublishReturned);
    }
}
