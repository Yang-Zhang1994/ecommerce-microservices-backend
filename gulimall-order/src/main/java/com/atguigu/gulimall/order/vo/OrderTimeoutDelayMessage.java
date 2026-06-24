package com.atguigu.gulimall.order.vo;

import lombok.Data;

/**
 * JSON body: {@code order.wait.pay} → delay queue; after TTL, redelivered as {@code order.release.order}.
 */
@Data
public class OrderTimeoutDelayMessage {
    private String orderSn;
}
