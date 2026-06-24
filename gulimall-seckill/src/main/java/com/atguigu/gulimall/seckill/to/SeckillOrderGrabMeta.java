package com.atguigu.gulimall.seckill.to;

import lombok.Data;

import java.io.Serializable;

/** Stored in Redis at grab time; used to roll back quota + semaphore when order is not paid. */
@Data
public class SeckillOrderGrabMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderSn;
    private Long memberId;
    private String killId;
    private String randomCode;
    private int num;
}
