package com.atguigu.common.constant;

/**
 * RabbitMQ topology shared by gulimall-seckill (publisher) and gulimall-order (consumer).
 */
public final class SeckillMqConstant {

    private SeckillMqConstant() {}

    public static final String SECKILL_EVENT_EXCHANGE = "seckill-event-exchange";

    public static final String SECKILL_ORDER_CREATE_QUEUE = "seckill.order.create.queue";

    public static final String ROUTING_KEY_SECKILL_ORDER_CREATE = "seckill.order.create";

    /** Unpaid seckill order closed → restore user quota + semaphore permits. */
    public static final String SECKILL_ORDER_RELEASE_QUEUE = "seckill.order.release.queue";

    public static final String ROUTING_KEY_SECKILL_ORDER_RELEASE = "seckill.order.release";
}
