package com.atguigu.gulimall.order.vo;

import lombok.Data;

@Data
public class OrderSubmitVo {
    private String orderToken;
    private Long addressId;
    private String note;
    private Integer payType;
}
