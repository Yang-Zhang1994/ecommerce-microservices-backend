package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderConfirmVo {
    private List<MemberAddressVo> addresses = new ArrayList<>();
    private List<OrderConfirmItemVo> items = new ArrayList<>();
    /**
     * Available points (placeholder for now; will be replaced with real member points later).
     */
    private Integer integration = 0;
    /**
     * Discount amount converted from integration points.
     */
    private BigDecimal integrationAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal freightAmount = BigDecimal.ZERO;
    private BigDecimal payPrice = BigDecimal.ZERO;
    private String orderToken;
}
