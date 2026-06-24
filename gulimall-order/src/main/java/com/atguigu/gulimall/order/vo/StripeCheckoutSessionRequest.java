package com.atguigu.gulimall.order.vo;

import lombok.Data;

@Data
public class StripeCheckoutSessionRequest {
    private String orderSn;
}
