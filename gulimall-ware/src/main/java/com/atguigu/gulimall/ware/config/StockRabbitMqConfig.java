package com.atguigu.gulimall.ware.config;

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
 * Declares the stock event topology: topic exchange, delay queue (TTL + DLX), and release queue.
 * <p>
 * Flow: lock success → publish routing key {@code stock.locked} → {@code stock.delay.queue} →
 * after TTL, dead-letter to the same exchange with key {@code stock.release} →
 * {@code stock.release.stock.queue} (bound with {@code stock.release.#}).
 * Order service can publish with {@code order.release.other.#} to the same release queue.
 */
@Configuration
@EnableRabbit
public class StockRabbitMqConfig {

    public static final String STOCK_EVENT_EXCHANGE = "stock-event-exchange";

    public static final String STOCK_DELAY_QUEUE = "stock.delay.queue";

    public static final String STOCK_RELEASE_QUEUE = "stock.release.stock.queue";

    /** After lock success, route into the delay queue. */
    public static final String ROUTING_KEY_STOCK_LOCKED = "stock.locked";

    /** When delay TTL expires, dead-letter to the exchange with this key (must match stock.release.#). */
    public static final String ROUTING_KEY_STOCK_RELEASE_DLX = "stock.release";

    public static final String ROUTING_KEY_STOCK_RELEASE_PATTERN = "stock.release.#";

    public static final String ROUTING_KEY_ORDER_RELEASE_OTHER_PATTERN = "order.release.other.#";

    @Value("${ware.stock.rabbit.delay-ttl-minutes:50}")
    private int delayTtlMinutes;

    @Bean
    public TopicExchange stockEventExchange() {
        return new TopicExchange(STOCK_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> args = new HashMap<>(4);
        args.put("x-dead-letter-exchange", STOCK_EVENT_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_KEY_STOCK_RELEASE_DLX);
        long ttlMs = Math.max(1, delayTtlMinutes) * 60L * 1000L;
        args.put("x-message-ttl", ttlMs);
        return new Queue(STOCK_DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue(STOCK_RELEASE_QUEUE, true);
    }

    @Bean
    public Binding stockLockedToDelayQueue() {
        return BindingBuilder.bind(stockDelayQueue())
                .to(stockEventExchange())
                .with(ROUTING_KEY_STOCK_LOCKED);
    }

    @Bean
    public Binding stockReleasePatternToReleaseQueue() {
        return BindingBuilder.bind(stockReleaseStockQueue())
                .to(stockEventExchange())
                .with(ROUTING_KEY_STOCK_RELEASE_PATTERN);
    }

    @Bean
    public Binding orderReleaseOtherToReleaseQueue() {
        return BindingBuilder.bind(stockReleaseStockQueue())
                .to(stockEventExchange())
                .with(ROUTING_KEY_ORDER_RELEASE_OTHER_PATTERN);
    }
}
