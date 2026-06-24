package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * Body for {@code stock.locked} → delay queue; after TTL, redelivered as {@code stock.release} for consumer.
 */
@Data
public class StockDelayMessage {
    private Long taskId;
    private String orderSn;
}
