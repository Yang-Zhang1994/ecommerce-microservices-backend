package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmItemVo {
    private Long skuId;
    private String title;
    private String image;
    private List<String> skuAttr;
    private Integer count;
    private BigDecimal price;
    private BigDecimal totalPrice;

    /** Populated from ware service sum(stock - stock_locked) > 0 on confirm; see OrderWorkflowServiceImpl */
    private Boolean hasStock;
}
