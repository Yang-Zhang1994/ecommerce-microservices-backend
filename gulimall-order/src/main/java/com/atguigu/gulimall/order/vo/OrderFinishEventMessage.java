package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Payment success → {@code order.finish.user} / {@code order.finish.ware}.
 */
@Data
public class OrderFinishEventMessage {

    private String orderSn;
    private Long memberId;
    private BigDecimal payAmount;
    /** Mirrors {@link com.atguigu.gulimall.order.enums.OrderStatusEnum#PAYED} after update */
    private Integer orderStatus;
}
