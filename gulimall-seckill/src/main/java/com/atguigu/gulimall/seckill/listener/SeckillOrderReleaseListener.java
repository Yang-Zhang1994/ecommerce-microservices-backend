package com.atguigu.gulimall.seckill.listener;

import com.atguigu.common.to.SeckillOrderReleaseTo;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.atguigu.common.constant.SeckillMqConstant.SECKILL_ORDER_RELEASE_QUEUE;

@Component
public class SeckillOrderReleaseListener {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderReleaseListener.class);

    private final SeckillService seckillService;
    private final ObjectMapper objectMapper;

    public SeckillOrderReleaseListener(SeckillService seckillService, ObjectMapper objectMapper) {
        this.seckillService = seckillService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = SECKILL_ORDER_RELEASE_QUEUE, ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String json = message.getBody() == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
            if (!StringUtils.hasText(json)) {
                ack(channel, deliveryTag);
                return;
            }
            SeckillOrderReleaseTo payload = objectMapper.readValue(json.trim(), SeckillOrderReleaseTo.class);
            if (payload != null && StringUtils.hasText(payload.getOrderSn())) {
                seckillService.rollbackUnpaidGrab(payload.getOrderSn().trim());
            }
            ack(channel, deliveryTag);
        } catch (Exception e) {
            log.warn("seckill-order-release failed body={}",
                    message.getBody() == null ? null : new String(message.getBody(), StandardCharsets.UTF_8), e);
            nackRequeue(channel, deliveryTag);
        }
    }

    private void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("seckill-order-release: basicAck failed deliveryTag={}", deliveryTag, e);
        }
    }

    private void nackRequeue(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("seckill-order-release: basicNack failed deliveryTag={}", deliveryTag, e);
        }
    }
}
