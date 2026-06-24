package com.atguigu.gulimall.member.listener;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.atguigu.gulimall.member.config.OrderFinishUserMqConfig.ORDER_FINISH_USER_QUEUE;

/**
 * Hook for growth/integration credit on pay success; extend with member statistics updates when needed.
 */
@Component
public class OrderFinishUserListener {

    private static final Logger log = LoggerFactory.getLogger(OrderFinishUserListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = ORDER_FINISH_USER_QUEUE, ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String json = message.getBody() == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(json);
            String orderSn = root.path("orderSn").asText(null);
            Long memberId = root.path("memberId").isNumber() ? root.path("memberId").longValue() : null;
            if (log.isInfoEnabled()) {
                log.info("order.finish.user: received orderSn={} memberId={}", orderSn, memberId);
            }
            ackSafe(channel, deliveryTag);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("order.finish.user: failed", e);
            }
            nackRequeue(channel, deliveryTag);
        }
    }

    private void ackSafe(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("order.finish.user: basicAck failed", e);
        }
    }

    private void nackRequeue(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("order.finish.user: basicNack failed", e);
        }
    }
}
