package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_home_subject")
public class HomeSubjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 专题名字 */
    @Column(name = "name")
    private String name;

    /** 专题标题 */
    @Column(name = "title")
    private String title;

    /** 专题副标题 */
    @Column(name = "sub_title")
    private String subTitle;

    /** 显示状态 */
    @Column(name = "status")
    private Integer status;

    /** 详情连接 */
    @Column(name = "url")
    private String url;

    /** 排序 */
    @Column(name = "sort")
    private Integer sort;

    /** 专题图片地址 */
    @Column(name = "img")
    private String img;
}
