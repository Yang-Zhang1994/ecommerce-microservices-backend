package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀活动场次
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_seckill_session")
public class SeckillSessionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 场次名称 */
    @Column(name = "name")
    private String name;

    /** 每日开始时间 */
    @Column(name = "start_time")
    private Date startTime;

    /** 每日结束时间 */
    @Column(name = "end_time")
    private Date endTime;

    /** 启用状态 */
    @Column(name = "status")
    private Integer status;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;
}
