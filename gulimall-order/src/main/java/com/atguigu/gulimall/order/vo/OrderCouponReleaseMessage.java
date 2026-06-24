package com.atguigu.gulimall.order.vo;

import lombok.Data;

/**
 * Cancel / close → {@code order.release.coupon.*}.
 */
@Data
public class OrderCouponReleaseMessage {

    private String orderSn;
    /** e.g. cancel, timeout */
    private String reason;
}
