package com.atguigu.gulimall.order.to;

import lombok.Data;

@Data
public class OrderLoginUserTo {
    private Long userId;
    private String username;
    private Integer integration;
}
