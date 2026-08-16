package com.atguigu.gulimall.ware.listener;

import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.repository.WareOrderTaskRepository;
import com.atguigu.gulimall.ware.service.WareSkuService;
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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.atguigu.gulimall.ware.config.OrderFinishWareMqConfig.ORDER_FINISH_WARE_QUEUE;

/**
 * Consumes {@code order.finish.ware.*} (order paid) and turns locked stock into a real deduction.
 * <p>
 * Delivery is at-least-once: idempotency comes from the work-order line status plus the
 * {@code stock_locked >= num} guard in the deduct statement, so a redelivered message deducts nothing.
 */
@Component
public class OrderFinishWareListener {

    private static final Logger log = LoggerFactory.getLogger(OrderFinishWareListener.class);

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WareOrderTaskRepository wareOrderTaskRepository;
    @Autowired
    private WareSkuService wareSkuService;

    @RabbitListener(queues = ORDER_FINISH_WARE_QUEUE, ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String json = message.getBody() == null ? "" : new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            String orderSn = readOrderSn(json);
            if (!StringUtils.hasText(orderSn)) {
                log.warn("order.finish.ware: no orderSn in payload, dropping body={}", json);
                ackSafe(channel, deliveryTag);
                return;
            }
            Optional<Long> taskId = wareOrderTaskRepository.findFirstByOrderSnOrderByIdDesc(orderSn)
                    .map(WareOrderTaskEntity::getId);
            if (taskId.isEmpty()) {
                log.warn("order.finish.ware: no stock work order for orderSn={}", orderSn);
                ackSafe(channel, deliveryTag);
                return;
            }
            int deducted = wareSkuService.deductByTaskId(taskId.get());
            if (log.isInfoEnabled()) {
                log.info(
                        "order.finish.ware: orderSn={} taskId={} deductedLines={}",
                        orderSn, taskId.get(), deducted);
            }
            ackSafe(channel, deliveryTag);
        } catch (Exception e) {
            log.warn("order.finish.ware: handle failed body={}", json, e);
            nackRequeue(channel, deliveryTag);
        }
    }

    private String readOrderSn(String json) throws IOException {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        JsonNode root = objectMapper.readTree(json);
        String orderSn = root.path("orderSn").asText(null);
        return StringUtils.hasText(orderSn) ? orderSn.trim() : null;
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
