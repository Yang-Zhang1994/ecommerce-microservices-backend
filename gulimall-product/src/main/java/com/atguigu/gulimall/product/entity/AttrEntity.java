package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品属性
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_attr")
public class AttrEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attr_id")
    private Long attrId;

    /** 属性名 */
    @Column(name = "attr_name")
    private String attrName;

    /** 是否需要检索[0-不需要，1-需要] */
    @Column(name = "search_type")
    private Integer searchType;

    /** 值类型[0-为单个值，1-可以选择多个值] */
    @Column(name = "value_type")
    private Integer valueType;

    /** 属性图标 */
    @Column(name = "icon")
    private String icon;

    /** 可选值列表[用逗号分隔] */
    @Column(name = "value_select")
    private String valueSelect;

    /** 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性] */
    @Column(name = "attr_type")
    private Integer attrType;

    /** 启用状态[0 - 禁用，1 - 启用] */
    @Column(name = "enable")
    private Long enable;

    /** 所属分类 */
    @Column(name = "catelog_id")
    private Long catelogId;

    /** 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整 */
    @Column(name = "show_desc")
    private Integer showDesc;
}
