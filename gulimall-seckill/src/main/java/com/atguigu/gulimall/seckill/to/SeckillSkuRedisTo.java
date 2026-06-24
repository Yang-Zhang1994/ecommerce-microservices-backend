package com.atguigu.gulimall.seckill.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One seckill SKU as warmed into Redis and returned to the storefront.
 */
@Data
public class SeckillSkuRedisTo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** sms_seckill_sku_relation primary key. */
    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private BigDecimal seckillCount;
    private BigDecimal seckillLimit;
    private Integer seckillSort;

    /** Resolved product info (title/image/original price). */
    private SkuInfoVo skuInfo;

    /** Session window (epoch millis). */
    private Long startTime;
    private Long endTime;

    /**
     * Random token that gates the kill endpoint. Only exposed to the storefront while the
     * session is live, so the buy URL cannot be guessed ahead of time.
     */
    private String randomCode;
}
