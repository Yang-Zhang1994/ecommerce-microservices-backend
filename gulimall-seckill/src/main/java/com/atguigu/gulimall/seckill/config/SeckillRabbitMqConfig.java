package com.atguigu.gulimall.seckill.config;

import com.atguigu.common.constant.SeckillMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the seckill ↔ order MQ topology (safe to declare from publisher side).
 */
@Configuration
@EnableRabbit
public class SeckillRabbitMqConfig {

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

    @Bean
    public Queue seckillOrderReleaseQueue() {
        return new Queue(SeckillMqConstant.SECKILL_ORDER_RELEASE_QUEUE, true);
    }

    @Bean
    public Binding seckillOrderReleaseBinding() {
        return BindingBuilder.bind(seckillOrderReleaseQueue())
                .to(seckillEventExchange())
                .with(SeckillMqConstant.ROUTING_KEY_SECKILL_ORDER_RELEASE);
    }
}
