package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitResponseVo {
    private Integer code;
    private String msg;
    private String orderSn;
    private BigDecimal payAmount;
}
