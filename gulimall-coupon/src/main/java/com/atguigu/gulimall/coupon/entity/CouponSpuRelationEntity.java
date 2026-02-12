package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券与产品关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_coupon_spu_relation")
public class CouponSpuRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 优惠券id */
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "spu_id")
    private Long spuId;

    @Column(name = "spu_name")
    private String spuName;
}
