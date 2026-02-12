package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀活动
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_seckill_promotion")
public class SeckillPromotionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 活动标题 */
    @Column(name = "title")
    private String title;

    /** 开始日期 */
    @Column(name = "start_time")
    private Date startTime;

    /** 结束日期 */
    @Column(name = "end_time")
    private Date endTime;

    /** 上下线状态 */
    @Column(name = "status")
    private Integer status;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 创建人 */
    @Column(name = "user_id")
    private Long userId;
}
