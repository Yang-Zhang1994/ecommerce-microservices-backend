package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderPaymentSummaryVo {
    /** Whether the order is considered paid (status + payment row). */
    private boolean paid;
    /** Human label, e.g. Stripe card. */
    private String payMethodLabel;
    /** Raw payment_status from oms_payment_info when present. */
    private String paymentStatus;
    private Date paymentTime;
    private BigDecimal paidAmount;
    /** External reference (Stripe payment intent / session id). */
    private String tradeNo;
}
