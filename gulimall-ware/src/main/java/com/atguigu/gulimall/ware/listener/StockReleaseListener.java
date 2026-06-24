package com.atguigu.gulimall.ware.listener;

import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.repository.WareOrderTaskRepository;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.StockDelayMessage;
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

import static com.atguigu.gulimall.ware.config.StockRabbitMqConfig.STOCK_RELEASE_QUEUE;

/**
 * Consumes stock.release.stock.queue: delay expiry (stock.release) or order cancel (order.release.other.*).
 * Unlocks by {@code taskId} (or resolves task from {@code orderSn}) using DB work-order details.
 */
@Component
public class StockReleaseListener {

    private static final Logger log = LoggerFactory.getLogger(StockReleaseListener.class);

    @Autowired
    private WareSkuService wareSkuService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WareOrderTaskRepository wareOrderTaskRepository;

    @RabbitListener(queues = STOCK_RELEASE_QUEUE, ackMode = "MANUAL")
    public void onMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            if (!StringUtils.hasText(json)) {
                ack(channel, deliveryTag);
                return;
            }
            StockDelayMessage payload = objectMapper.readValue(json.trim(), StockDelayMessage.class);
            if (payload.getTaskId() != null) {
                wareSkuService.unlockByTaskId(payload.getTaskId());
                ack(channel, deliveryTag);
                return;
            }
            if (StringUtils.hasText(payload.getOrderSn())) {
                wareOrderTaskRepository.findFirstByOrderSnOrderByIdDesc(payload.getOrderSn().trim())
                        .map(WareOrderTaskEntity::getId)
                        .ifPresentOrElse(
                                wareSkuService::unlockByTaskId,
                                () -> log.warn("stock-release: no task for orderSn={}", payload.getOrderSn())
                        );
            }
            ack(channel, deliveryTag);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("stock-release: handle failed body={}", message.getBody() == null ? null : new String(message.getBody(), StandardCharsets.UTF_8), e);
            }
            nackRequeue(channel, deliveryTag);
        }
    }

    private void ack(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("stock-release: basicAck failed deliveryTag={}", deliveryTag, e);
        }
    }

    private void nackRequeue(Channel channel, long deliveryTag) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            log.error("stock-release: basicNack(requeue=true) failed deliveryTag={}", deliveryTag, e);
        }
    }
}
