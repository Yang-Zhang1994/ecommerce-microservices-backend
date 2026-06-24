package com.atguigu.gulimall.ware.listener;

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

import static com.atguigu.gulimall.ware.config.OrderFinishWareMqConfig.ORDER_FINISH_WARE_QUEUE;

/**
 * Placeholder for diagram step “inventory check / split”; extend with split-order logic when needed.
 */
@Component
public class OrderFinishWareListener {

    private static final Logger log = LoggerFactory.getLogger(OrderFinishWareListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = ORDER_FINISH_WARE_QUEUE, ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String json = message.getBody() == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(json);
            String orderSn = root.path("orderSn").asText(null);
            if (log.isInfoEnabled()) {
                log.info("order.finish.ware: received orderSn={}", orderSn);
            }
            ackSafe(channel, deliveryTag);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("order.finish.ware: failed", e);
            }
            nackRequeue(channel, deliveryTag);
        }
    }

    private void ackSafe(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("order.finish.ware: basicAck failed", e);
        }
    }

    private void nackRequeue(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("order.finish.ware: basicNack failed", e);
        }
    }
}
