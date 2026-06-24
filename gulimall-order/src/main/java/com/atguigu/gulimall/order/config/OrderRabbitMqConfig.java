package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Order-event topology: timeout close, pay-success fan-out, coupon release on cancel.
 * <p>
 * Order close delay is {@code order.pay-timeout-minutes} (optional to match ware stock TTL).
 */
@Configuration
@EnableRabbit
public class OrderRabbitMqConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";

    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";

    public static final String ORDER_RELEASE_ORDER_QUEUE = "order.release.order.queue";

    public static final String ORDER_FINISH_USER_QUEUE = "order.finish.user.queue";

    public static final String ORDER_FINISH_WARE_QUEUE = "order.finish.ware.queue";

    public static final String ORDER_RELEASE_COUPON_QUEUE = "order.release.coupon.queue";

    /** Route into the delay queue after submit (same topology pattern as ware stock lock). */
    public static final String ROUTING_KEY_ORDER_WAIT_PAY = "order.wait.pay";

    /** After TTL, dead-letter to the exchange with this key → {@link #ORDER_RELEASE_ORDER_QUEUE}. */
    public static final String ROUTING_KEY_ORDER_RELEASE_ORDER_DLX = "order.release.order";

    /** Consumer queue binds with this pattern (matches DLX routing key). */
    public static final String ROUTING_KEY_ORDER_RELEASE_ORDER_PATTERN = "order.release.order.#";

    /** Payment success → member (points, etc.). */
    public static final String ROUTING_KEY_ORDER_FINISH_USER = "order.finish.user";

    public static final String ROUTING_KEY_ORDER_FINISH_USER_PATTERN = "order.finish.user.#";

    /** Payment success → ware (split / downstream stock). */
    public static final String ROUTING_KEY_ORDER_FINISH_WARE = "order.finish.ware";

    public static final String ROUTING_KEY_ORDER_FINISH_WARE_PATTERN = "order.finish.ware.#";

    /** Cancel / timeout → coupon service (return locks). */
    public static final String ROUTING_KEY_ORDER_RELEASE_COUPON_PATTERN = "order.release.coupon.#";

    /** Seckill orders use a shorter unpaid timeout (flash_order_overtime). */
    public static final String ORDER_SECKILL_DELAY_QUEUE = "order.seckill.delay.queue";

    public static final String ROUTING_KEY_ORDER_SECKILL_WAIT_PAY = "order.seckill.wait.pay";

    @Value("${order.pay-timeout-minutes:30}")
    private int payTimeoutMinutes;

    @Value("${order.flash-pay-timeout-minutes:15}")
    private int flashPayTimeoutMinutes;

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    /** Same exchange name as ware; safe to declare from order so cancel can publish before ware starts. */
    @Bean
    public TopicExchange stockEventExchangeAlias() {
        return new TopicExchange(OrderStockMqConstants.STOCK_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-dead-letter-exchange", ORDER_EVENT_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_KEY_ORDER_RELEASE_ORDER_DLX);
        long ttlMs = Math.max(1, payTimeoutMinutes) * 60L * 1000L;
        args.put("x-message-ttl", ttlMs);
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, args);
    }

    /**
     * Seckill unpaid timeout uses <strong>per-message</strong> {@code expiration} (see
     * {@link com.atguigu.gulimall.order.service.MqOutboxDispatchService}) so each order gets its
     * own {@code flash-pay-timeout-minutes} window instead of sharing one queue-level TTL.
     */
    @Bean
    public Queue orderSeckillDelayQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-dead-letter-exchange", ORDER_EVENT_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_KEY_ORDER_RELEASE_ORDER_DLX);
        return new Queue(ORDER_SECKILL_DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue(ORDER_RELEASE_ORDER_QUEUE, true);
    }

    @Bean
    public Binding orderWaitPayToDelayQueue() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderEventExchange())
                .with(ROUTING_KEY_ORDER_WAIT_PAY);
    }

    @Bean
    public Binding orderSeckillWaitPayToDelayQueue() {
        return BindingBuilder.bind(orderSeckillDelayQueue())
                .to(orderEventExchange())
                .with(ROUTING_KEY_ORDER_SECKILL_WAIT_PAY);
    }

    @Bean
    public Binding orderReleaseOrderPatternToReleaseQueue() {
        return BindingBuilder.bind(orderReleaseOrderQueue())
                .to(orderEventExchange())
                .with(ROUTING_KEY_ORDER_RELEASE_ORDER_PATTERN);
    }

    @Bean
    public Queue orderFinishUserQueue() {
        return new Queue(ORDER_FINISH_USER_QUEUE, true);
    }

    @Bean
    public Queue orderFinishWareQueue() {
        return new Queue(ORDER_FINISH_WARE_QUEUE, true);
    }

    @Bean
    public Queue orderReleaseCouponQueue() {
        return new Queue(ORDER_RELEASE_COUPON_QUEUE, true);
    }

    @Bean
    public Binding orderFinishUserPatternToQueue() {
        return BindingBuilder.bind(orderFinishUserQueue())
                .to(orderEventExchange())
                .with(ROUTING_KEY_ORDER_FINISH_USER_PATTERN);
    }

    @Bean
    public Binding orderFinishWarePatternToQueue() {
        return BindingBuilder.bind(orderFinishWareQueue())
                .to(orderEventExchange())
                .with(ROUTING_KEY_ORDER_FINISH_WARE_PATTERN);
    }

    @Bean
    public Binding orderReleaseCouponPatternToQueue() {
        return BindingBuilder.bind(orderReleaseCouponQueue())
                .to(orderEventExchange())
                .with(ROUTING_KEY_ORDER_RELEASE_COUPON_PATTERN);
    }
}
