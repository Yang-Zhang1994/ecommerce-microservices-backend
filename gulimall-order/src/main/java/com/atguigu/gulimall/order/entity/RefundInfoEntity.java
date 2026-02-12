package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_refund_info")
public class RefundInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 退款的订单 */
    @Column(name = "order_return_id")
    private Long orderReturnId;

    /** 退款金额 */
    @Column(name = "refund")
    private BigDecimal refund;

    /** 退款交易流水号 */
    @Column(name = "refund_sn")
    private String refundSn;

    /** 退款状态 */
    @Column(name = "refund_status")
    private Integer refundStatus;

    /** 退款渠道[1-支付宝，2-微信，3-银联，4-汇款] */
    @Column(name = "refund_channel")
    private Integer refundChannel;

    @Column(name = "refund_content")
    private String refundContent;
}
