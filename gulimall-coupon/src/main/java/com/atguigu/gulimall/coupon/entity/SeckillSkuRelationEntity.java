package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀活动商品关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_seckill_sku_relation")
public class SeckillSkuRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 活动id */
    @Column(name = "promotion_id")
    private Long promotionId;

    /** 活动场次id */
    @Column(name = "promotion_session_id")
    private Long promotionSessionId;

    /** 商品id */
    @Column(name = "sku_id")
    private Long skuId;

    /** 秒杀价格 */
    @Column(name = "seckill_price")
    private BigDecimal seckillPrice;

    /** 秒杀总量 */
    @Column(name = "seckill_count")
    private BigDecimal seckillCount;

    /** 每人限购数量 */
    @Column(name = "seckill_limit")
    private BigDecimal seckillLimit;

    /** 排序 */
    @Column(name = "seckill_sort")
    private Integer seckillSort;
}
