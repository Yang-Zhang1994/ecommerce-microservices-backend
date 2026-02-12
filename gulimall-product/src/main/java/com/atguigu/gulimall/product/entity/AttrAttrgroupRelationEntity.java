package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性&属性分组关联
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_attr_attrgroup_relation")
public class AttrAttrgroupRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 属性id */
    @Column(name = "attr_id")
    private Long attrId;

    /** 属性分组id */
    @Column(name = "attr_group_id")
    private Long attrGroupId;

    /** 属性组内排序 */
    @Column(name = "attr_sort")
    private Integer attrSort;
}
