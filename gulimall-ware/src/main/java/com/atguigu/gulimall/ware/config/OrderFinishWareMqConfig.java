package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consumes payment-success messages from order-service ({@code order.finish.ware.*}).
 * Exchange/queue names align with {@code OrderRabbitMqConfig} in gulimall-order.
 */
@Configuration
public class OrderFinishWareMqConfig {

    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";

    public static final String ORDER_FINISH_WARE_QUEUE = "order.finish.ware.queue";

    public static final String ROUTING_KEY_ORDER_FINISH_WARE_PATTERN = "order.finish.ware.#";

    @Bean
    public TopicExchange orderEventExchangeBean() {
        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderFinishWareQueueBean() {
        return new Queue(ORDER_FINISH_WARE_QUEUE, true);
    }

    @Bean
    public Binding orderFinishWareBindingBean() {
        return BindingBuilder.bind(orderFinishWareQueueBean())
                .to(orderEventExchangeBean())
                .with(ROUTING_KEY_ORDER_FINISH_WARE_PATTERN);
    }
}
