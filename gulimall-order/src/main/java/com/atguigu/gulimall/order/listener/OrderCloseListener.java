package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderTimeoutDelayMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.atguigu.gulimall.order.config.OrderRabbitMqConfig.ORDER_RELEASE_ORDER_QUEUE;

/**
 * Close unpaid orders when delay TTL expires ({@code order.pay-timeout-minutes}).
 */
@Component
public class OrderCloseListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCloseListener.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = ORDER_RELEASE_ORDER_QUEUE, ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String json = message.getBody() == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
            if (!StringUtils.hasText(json)) {
                ack(channel, deliveryTag);
                return;
            }
            OrderTimeoutDelayMessage payload = objectMapper.readValue(json.trim(), OrderTimeoutDelayMessage.class);
            if (!StringUtils.hasText(payload.getOrderSn())) {
                ack(channel, deliveryTag);
                return;
            }
            orderService.closeUnpaidOrderByOrderSn(payload.getOrderSn().trim());
            ack(channel, deliveryTag);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("order-close: failed body={}", message.getBody() == null ? null : new String(message.getBody(), StandardCharsets.UTF_8), e);
            }
            nackRequeue(channel, deliveryTag);
        }
    }

    private void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("order-close: basicAck failed deliveryTag={}", deliveryTag, e);
        }
    }

    private void nackRequeue(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("order-close: basicNack(requeue=true) failed deliveryTag={}", deliveryTag, e);
        }
    }
}
