package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品满减信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_sku_full_reduction")
public class SkuFullReductionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    /** 满多少 */
    @Column(name = "full_price")
    private BigDecimal fullPrice;

    /** 减多少 */
    @Column(name = "reduce_price")
    private BigDecimal reducePrice;

    /** 是否参与其他优惠 */
    @Column(name = "add_other")
    private Integer addOther;
}
