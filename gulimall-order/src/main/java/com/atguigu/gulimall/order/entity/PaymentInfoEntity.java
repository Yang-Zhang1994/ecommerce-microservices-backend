package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付信息表
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_payment_info")
public class PaymentInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 订单号（对外业务号） */
    @Column(name = "order_sn")
    private String orderSn;

    /** 订单id */
    @Column(name = "order_id")
    private Long orderId;

    /** 支付宝交易流水号 */
    @Column(name = "alipay_trade_no")
    private String alipayTradeNo;

    /** 支付总金额 */
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    /** 交易内容 */
    @Column(name = "subject")
    private String subject;

    /** 支付状态 */
    @Column(name = "payment_status")
    private String paymentStatus;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 确认时间 */
    @Column(name = "confirm_time")
    private Date confirmTime;

    /** 回调内容 */
    @Column(name = "callback_content")
    private String callbackContent;

    /** 回调时间 */
    @Column(name = "callback_time")
    private Date callbackTime;
}
