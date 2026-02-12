package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券分类关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_coupon_spu_category_relation")
public class CouponSpuCategoryRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 优惠券id */
    @Column(name = "coupon_id")
    private Long couponId;

    /** 产品分类id */
    @Column(name = "category_id")
    private Long categoryId;

    /** 产品分类名称 */
    @Column(name = "category_name")
    private String categoryName;
}
