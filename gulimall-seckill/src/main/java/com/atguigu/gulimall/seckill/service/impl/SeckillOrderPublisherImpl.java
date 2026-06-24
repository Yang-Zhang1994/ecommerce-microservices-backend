package com.atguigu.gulimall.seckill.service.impl;

import com.atguigu.common.constant.SeckillMqConstant;
import com.atguigu.common.to.SeckillOrderCreateTo;
import com.atguigu.gulimall.seckill.service.SeckillOrderPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderPublisherImpl implements SeckillOrderPublisher {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderPublisherImpl.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public SeckillOrderPublisherImpl(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishOrderCreate(SeckillOrderCreateTo order) {
        try {
            String json = objectMapper.writeValueAsString(order);
            rabbitTemplate.convertAndSend(
                    SeckillMqConstant.SECKILL_EVENT_EXCHANGE,
                    SeckillMqConstant.ROUTING_KEY_SECKILL_ORDER_CREATE,
                    json
            );
            log.info("Published seckill order create: orderSn={}, memberId={}, skuId={}",
                    order.getOrderSn(), order.getMemberId(), order.getSkuId());
        } catch (Exception e) {
            log.error("Failed to publish seckill order orderSn={}", order.getOrderSn(), e);
            throw new IllegalStateException("Failed to enqueue seckill order", e);
        }
    }
}
