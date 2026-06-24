package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailVo {
    private Long id;
    private String orderSn;
    private Integer status;
    private String statusText;
    private Date createTime;
    private Date paymentTime;
    private Date deliveryTime;
    private Date receiveTime;

    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    private String note;

    private String deliveryCompany;
    private String deliverySn;

    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;

    private List<OrderDetailItemVo> items;
    private OrderPaymentSummaryVo payment;
}
