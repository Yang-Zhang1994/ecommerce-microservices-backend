package com.atguigu.gulimall.member.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Payment success → points / growth (aligns with order-service {@code order.finish.user.*}).
 */
@Configuration
@EnableRabbit
public class OrderFinishUserMqConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";

    public static final String ORDER_FINISH_USER_QUEUE = "order.finish.user.queue";

    public static final String ROUTING_KEY_ORDER_FINISH_USER_PATTERN = "order.finish.user.#";

    @Bean
    public TopicExchange orderEventExchangeForMember() {
        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderFinishUserQueueBean() {
        return new Queue(ORDER_FINISH_USER_QUEUE, true);
    }

    @Bean
    public Binding orderFinishUserBindingBean() {
        return BindingBuilder.bind(orderFinishUserQueueBean())
                .to(orderEventExchangeForMember())
                .with(ROUTING_KEY_ORDER_FINISH_USER_PATTERN);
    }
}
