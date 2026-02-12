package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品会员价格
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_member_price")
public class MemberPriceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    /** 会员等级id */
    @Column(name = "member_level_id")
    private Long memberLevelId;

    /** 会员等级名 */
    @Column(name = "member_level_name")
    private String memberLevelName;

    /** 会员对应价格 */
    @Column(name = "member_price")
    private BigDecimal memberPrice;

    /** 可否叠加其他优惠[0-不可叠加优惠，1-可叠加] */
    @Column(name = "add_other")
    private Integer addOther;
}
