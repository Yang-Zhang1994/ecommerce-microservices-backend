package com.atguigu.gulimall.order.service;

import com.atguigu.common.constant.SeckillMqConstant;
import com.atguigu.common.to.SeckillOrderReleaseTo;
import com.atguigu.gulimall.order.config.OrderRabbitMqConfig;
import com.atguigu.gulimall.order.config.OrderStockMqConstants;
import com.atguigu.gulimall.order.entity.MqMessageEntity;
import com.atguigu.gulimall.order.repository.MqMessageRepository;
import com.atguigu.gulimall.order.vo.OrderCouponReleaseMessage;
import com.atguigu.gulimall.order.vo.OrderFinishEventMessage;
import com.atguigu.gulimall.order.vo.OrderTimeoutDelayMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Inserts outbox rows in the <strong>same transaction</strong> as order mutations.
 * Downstream consumers must treat messages as at-least-once (idempotent handlers).
 */
@Service
public class OrderMqOutboxService {

    @Autowired
    private MqMessageRepository mqMessageRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public void enqueueOrderWaitPayDelay(String orderSn) {
        try {
            OrderTimeoutDelayMessage msg = new OrderTimeoutDelayMessage();
            msg.setOrderSn(orderSn);
            String json = objectMapper.writeValueAsString(msg);
            savePending(
                    json,
                    OrderRabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    OrderRabbitMqConfig.ROUTING_KEY_ORDER_WAIT_PAY,
                    OrderTimeoutDelayMessage.class.getName()
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue order.wait.pay failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void enqueueSeckillOrderWaitPayDelay(String orderSn) {
        try {
            OrderTimeoutDelayMessage msg = new OrderTimeoutDelayMessage();
            msg.setOrderSn(orderSn);
            String json = objectMapper.writeValueAsString(msg);
            savePending(
                    json,
                    OrderRabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    OrderRabbitMqConfig.ROUTING_KEY_ORDER_SECKILL_WAIT_PAY,
                    OrderTimeoutDelayMessage.class.getName()
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue order.seckill.wait.pay failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void enqueueSeckillGrabRelease(String orderSn) {
        try {
            SeckillOrderReleaseTo msg = new SeckillOrderReleaseTo();
            msg.setOrderSn(orderSn);
            String json = objectMapper.writeValueAsString(msg);
            savePending(
                    json,
                    SeckillMqConstant.SECKILL_EVENT_EXCHANGE,
                    SeckillMqConstant.ROUTING_KEY_SECKILL_ORDER_RELEASE,
                    SeckillOrderReleaseTo.class.getName()
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue seckill.order.release failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void enqueueStockUnlockForCancel(String orderSn) {
        try {
            Map<String, Object> body = new LinkedHashMap<>(2);
            body.put("orderSn", orderSn);
            String json = objectMapper.writeValueAsString(body);
            savePending(
                    json,
                    OrderStockMqConstants.STOCK_EVENT_EXCHANGE,
                    OrderStockMqConstants.ROUTING_KEY_ORDER_RELEASE_OTHER_ORDER,
                    "java.util.Map"
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue stock unlock failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void enqueueCouponRelease(OrderCouponReleaseMessage payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            savePending(
                    json,
                    OrderRabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    "order.release.coupon.cancel",
                    OrderCouponReleaseMessage.class.getName()
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue coupon release failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void enqueueOrderFinishUser(OrderFinishEventMessage payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            savePending(
                    json,
                    OrderRabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    OrderRabbitMqConfig.ROUTING_KEY_ORDER_FINISH_USER,
                    OrderFinishEventMessage.class.getName()
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue order.finish.user failed", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void enqueueOrderFinishWare(OrderFinishEventMessage payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            savePending(
                    json,
                    OrderRabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    OrderRabbitMqConfig.ROUTING_KEY_ORDER_FINISH_WARE,
                    OrderFinishEventMessage.class.getName()
            );
        } catch (Exception e) {
            throw new IllegalStateException("outbox enqueue order.finish.ware failed", e);
        }
    }

    private void savePending(String content, String exchange, String routingKey, String classType) {
        MqMessageEntity row = new MqMessageEntity();
        row.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        row.setContent(content);
        row.setToExchange(exchange);
        row.setRoutingKey(routingKey);
        row.setClassType(classType);
        row.setMessageStatus(MqMessageEntity.STATUS_NEW);
        mqMessageRepository.save(row);
    }
}
