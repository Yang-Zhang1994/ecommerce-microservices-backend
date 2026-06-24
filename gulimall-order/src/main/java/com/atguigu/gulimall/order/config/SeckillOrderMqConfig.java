package com.atguigu.gulimall.order.config;

import com.atguigu.common.constant.SeckillMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seckill → order MQ topology (consumer side; idempotent declare).
 */
@Configuration
public class SeckillOrderMqConfig {

    @Bean
    public TopicExchange seckillEventExchange() {
        return new TopicExchange(SeckillMqConstant.SECKILL_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue seckillOrderCreateQueue() {
        return new Queue(SeckillMqConstant.SECKILL_ORDER_CREATE_QUEUE, true);
    }

    @Bean
    public Binding seckillOrderCreateBinding() {
        return BindingBuilder.bind(seckillOrderCreateQueue())
                .to(seckillEventExchange())
                .with(SeckillMqConstant.ROUTING_KEY_SECKILL_ORDER_CREATE);
    }
}
