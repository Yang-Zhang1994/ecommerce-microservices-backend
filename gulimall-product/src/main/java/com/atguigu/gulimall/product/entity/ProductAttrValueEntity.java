package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * spu属性值
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_product_attr_value")
public class ProductAttrValueEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 商品id */
    @Column(name = "spu_id")
    private Long spuId;

    /** 属性id */
    @Column(name = "attr_id")
    private Long attrId;

    /** 属性名 */
    @Column(name = "attr_name")
    private String attrName;

    /** 属性值 */
    @Column(name = "attr_value")
    private String attrValue;

    /** 顺序 */
    @Column(name = "attr_sort")
    private Integer attrSort;

    /** 快速展示【是否展示在介绍上；0-否 1-是】 */
    @Column(name = "quick_show")
    private Integer quickShow;
}
