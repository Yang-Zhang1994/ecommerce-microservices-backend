package com.atguigu.gulimall.coupon.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:06:56
 */
@Data
@Entity
@Table(name = "sms_coupon")
public class CouponEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 优惠卷类型[0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券] */
    @Column(name = "coupon_type")
    private Integer couponType;

    /** 优惠券图片 */
    @Column(name = "coupon_img")
    private String couponImg;

    /** 优惠卷名字 */
    @Column(name = "coupon_name")
    private String couponName;

    /** 数量 */
    @Column(name = "num")
    private Integer num;

    /** 金额 */
    @Column(name = "amount")
    private BigDecimal amount;

    /** 每人限领张数 */
    @Column(name = "per_limit")
    private Integer perLimit;

    /** 使用门槛 */
    @Column(name = "min_point")
    private BigDecimal minPoint;

    /** 开始时间 */
    @Column(name = "start_time")
    private Date startTime;

    /** 结束时间 */
    @Column(name = "end_time")
    private Date endTime;

    /** 使用类型[0->全场通用；1->指定分类；2->指定商品] */
    @Column(name = "use_type")
    private Integer useType;

    /** 备注 */
    @Column(name = "note")
    private String note;

    /** 发行数量 */
    @Column(name = "publish_count")
    private Integer publishCount;

    /** 已使用数量 */
    @Column(name = "use_count")
    private Integer useCount;

    /** 领取数量 */
    @Column(name = "receive_count")
    private Integer receiveCount;

    /** 可以领取的开始日期 */
    @Column(name = "enable_start_time")
    private Date enableStartTime;

    /** 可以领取的结束日期 */
    @Column(name = "enable_end_time")
    private Date enableEndTime;

    /** 优惠码 */
    @Column(name = "code")
    private String code;

    /** 可以领取的会员等级[0->不限等级，其他-对应等级] */
    @Column(name = "member_level")
    private Integer memberLevel;

    /** 发布状态[0-未发布，1-已发布] */
    @Column(name = "publish")
    private Integer publish;
}
