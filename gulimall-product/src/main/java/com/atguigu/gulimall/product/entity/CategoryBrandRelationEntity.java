package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 品牌分类关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_category_brand_relation")
public class CategoryBrandRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 品牌id */
    @Column(name = "brand_id")
    private Long brandId;

    /** 分类id */
    @Column(name = "catelog_id")
    private Long catelogId;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "catelog_name")
    private String catelogName;
}
