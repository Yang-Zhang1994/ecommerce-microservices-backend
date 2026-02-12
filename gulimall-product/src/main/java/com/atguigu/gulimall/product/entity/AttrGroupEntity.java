package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性分组
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_attr_group")
public class AttrGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attr_group_id")
    private Long attrGroupId;

    /** 组名 */
    @Column(name = "attr_group_name")
    private String attrGroupName;

    /** 排序 */
    @Column(name = "sort")
    private Integer sort;

    /** 描述 */
    @Column(name = "descript")
    private String descript;

    /** 组图标 */
    @Column(name = "icon")
    private String icon;

    /** 所属分类id */
    @Column(name = "catelog_id")
    private Long catelogId;

    /** 分类路径 */
    @Transient
    private Long[] catelogPath;
}
