package com.atguigu.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * MQ payload: seckill service → order service after a successful semaphore acquire.
 */
@Data
public class SeckillOrderCreateTo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderSn;
    private Long memberId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
}
