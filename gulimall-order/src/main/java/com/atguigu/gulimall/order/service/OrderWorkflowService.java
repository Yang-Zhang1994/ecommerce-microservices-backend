package com.atguigu.gulimall.order.service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitResponseVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SeckillOrderBindVo;
import com.atguigu.gulimall.order.vo.SeckillOrderConfirmVo;

public interface OrderWorkflowService {
    OrderConfirmVo confirmOrder(String cookieHeader);

    OrderSubmitResponseVo submitOrder(OrderSubmitVo submitVo, String cookieHeader);

    /** Pending seckill order: line items, addresses, pay amount. */
    SeckillOrderConfirmVo confirmSeckillOrder(String orderSn, String cookieHeader);

    /** Attach shipping address to a pending seckill order before payment. */
    void bindSeckillOrderAddress(SeckillOrderBindVo bindVo, String cookieHeader);

    /** Pending-payment cancel: CLOSED + MQ stock/coupon (requires login). */
    R cancelOrder(String orderSn, String cookieHeader);

    /** Pay callback / dev helper: {@code PAID} + {@code order.finish.*} (idempotent). */
    R notifyPaySuccess(String orderSn);
}
