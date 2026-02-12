package com.atguigu.gulimall.product.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.List;

/**
 * 商品三级分类
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_category")
@Where(clause = "show_status = 1")
public class CategoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cat_id")
    private Long catId;

    /** 分类名称 */
    @Column(name = "name")
    private String name;

    /** 父分类id */
    @Column(name = "parent_cid")
    private Long parentCid;

    /** 层级 */
    @Column(name = "cat_level")
    private Integer catLevel;

    /** 是否显示[0-不显示，1显示] */
    @Column(name = "show_status")
    private Integer showStatus;

    /** 排序 */
    @Column(name = "sort")
    private Integer sort;

    /** 图标地址 */
    @Column(name = "icon")
    private String icon;

    /** 计量单位 */
    @Column(name = "product_unit")
    private String productUnit;

    /** 商品数量 */
    @Column(name = "product_count")
    private Integer productCount;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Transient
    private List<CategoryEntity> children;

    /** 分类路径 [一级, 二级, 三级] */
    @Transient
    private Long[] catelogPath;
}
