package com.atguigu.common.to;

import lombok.Data;

import java.io.Serializable;

/** Order service → seckill: unpaid close/cancel, restore grab quota + Redis stock. */
@Data
public class SeckillOrderReleaseTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderSn;
}
