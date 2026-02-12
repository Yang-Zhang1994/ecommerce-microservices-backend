package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀商品通知订阅
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_seckill_sku_notice")
public class SeckillSkuNoticeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "sku_id")
    private Long skuId;

    /** 活动场次id */
    @Column(name = "session_id")
    private Long sessionId;

    /** 订阅时间 */
    @Column(name = "subcribe_time")
    private Date subcribeTime;

    /** 发送时间 */
    @Column(name = "send_time")
    private Date sendTime;

    /** 通知方式[0-短信，1-邮件] */
    @Column(name = "notice_type")
    private Integer noticeType;
}
