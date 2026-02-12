package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券领取历史记录
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_coupon_history")
public class CouponHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 优惠券id */
    @Column(name = "coupon_id")
    private Long couponId;

    /** 会员id */
    @Column(name = "member_id")
    private Long memberId;

    /** 会员名字 */
    @Column(name = "member_nick_name")
    private String memberNickName;

    /** 获取方式[0->后台赠送；1->主动领取] */
    @Column(name = "get_type")
    private Integer getType;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 使用状态[0->未使用；1->已使用；2->已过期] */
    @Column(name = "use_type")
    private Integer useType;

    /** 使用时间 */
    @Column(name = "use_time")
    private Date useTime;

    /** 订单id */
    @Column(name = "order_id")
    private Long orderId;

    /** 订单号 */
    @Column(name = "order_sn")
    private Long orderSn;
}
