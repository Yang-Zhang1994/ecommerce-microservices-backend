package com.atguigu.gulimall.order.config;

/**
 * Cross-service stock exchange (same name as {@code StockRabbitMqConfig} in ware). Used when order publishes unlock on cancel.
 */
public final class OrderStockMqConstants {

    private OrderStockMqConstants() {}

    public static final String STOCK_EVENT_EXCHANGE = "stock-event-exchange";

    /** Matches {@code StockRabbitMqConfig#ROUTING_KEY_ORDER_RELEASE_OTHER_PATTERN}: notify {@code stock.release.stock.queue}. */
    public static final String ROUTING_KEY_ORDER_RELEASE_OTHER_ORDER = "order.release.other.order";
}
