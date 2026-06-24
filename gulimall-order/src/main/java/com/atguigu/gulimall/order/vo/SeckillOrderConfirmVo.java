package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Flash-sale checkout: pending order + member addresses before payment. */
@Data
public class SeckillOrderConfirmVo {
    private String orderSn;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private Date createTime;
    /** True when receiver fields are still empty on the pending order. */
    private boolean needsAddress;
    private int flashPayTimeoutMinutes;
    private List<MemberAddressVo> addresses = new ArrayList<>();
    private List<OrderDetailItemVo> items = new ArrayList<>();
    /** Selected / saved receiver snapshot (may be empty before bind). */
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
}
