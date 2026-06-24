package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderWareLockVo {
    private String orderSn;
    private List<OrderItemLockVo> locks = new ArrayList<>();
}

