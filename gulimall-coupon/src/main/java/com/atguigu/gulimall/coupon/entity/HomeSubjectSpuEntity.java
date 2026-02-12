package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 专题商品
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_home_subject_spu")
public class HomeSubjectSpuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 专题名字 */
    @Column(name = "name")
    private String name;

    /** 专题id */
    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "spu_id")
    private Long spuId;

    /** 排序 */
    @Column(name = "sort")
    private Integer sort;
}
