package com.atguigu.gulimall.seckill.service;

import com.atguigu.common.to.SeckillOrderCreateTo;

public interface SeckillOrderPublisher {

    /** Emit order creation to gulimall-order via RabbitMQ (async落库 in order service). */
    void publishOrderCreate(SeckillOrderCreateTo order);
}
