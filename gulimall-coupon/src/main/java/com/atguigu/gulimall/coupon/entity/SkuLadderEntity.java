package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品阶梯价格
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_sku_ladder")
public class SkuLadderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    /** 满几件 */
    @Column(name = "full_count")
    private Integer fullCount;

    /** 打几折 */
    @Column(name = "discount")
    private BigDecimal discount;

    /** 折后价 */
    @Column(name = "price")
    private BigDecimal price;

    /** 是否叠加其他优惠[0-不可叠加，1-可叠加] */
    @Column(name = "add_other")
    private Integer addOther;
}
