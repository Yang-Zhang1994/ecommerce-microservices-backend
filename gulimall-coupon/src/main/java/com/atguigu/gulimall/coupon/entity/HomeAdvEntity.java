package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 首页轮播广告
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_home_adv")
public class HomeAdvEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 名字 */
    @Column(name = "name")
    private String name;

    /** 图片地址 */
    @Column(name = "pic")
    private String pic;

    /** 开始时间 */
    @Column(name = "start_time")
    private Date startTime;

    /** 结束时间 */
    @Column(name = "end_time")
    private Date endTime;

    /** 状态 */
    @Column(name = "status")
    private Integer status;

    /** 点击数 */
    @Column(name = "click_count")
    private Integer clickCount;

    /** 广告详情连接地址 */
    @Column(name = "url")
    private String url;

    /** 备注 */
    @Column(name = "note")
    private String note;

    /** 排序 */
    @Column(name = "sort")
    private Integer sort;

    /** 发布者 */
    @Column(name = "publisher_id")
    private Long publisherId;

    /** 审核者 */
    @Column(name = "auth_id")
    private Long authId;
}
