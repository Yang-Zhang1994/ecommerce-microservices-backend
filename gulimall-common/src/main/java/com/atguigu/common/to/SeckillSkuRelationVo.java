package com.atguigu.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** Seckill SKU relation row exposed to gulimall-seckill via HTTP. */
@Data
public class SeckillSkuRelationVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private BigDecimal seckillCount;
    private BigDecimal seckillLimit;
    private Integer seckillSort;
}
