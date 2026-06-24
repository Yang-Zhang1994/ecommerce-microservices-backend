package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailItemVo {
    private Long skuId;
    private String skuName;
    private String skuPic;
    private BigDecimal skuPrice;
    private Integer skuQuantity;
    private List<String> skuAttrs;
    private BigDecimal realAmount;
    private String spuName;
}
