package com.atguigu.gulimall.ware.vo;

import lombok.Data;

@Data
public class LockedStockVo {
    private Long skuId;
    private Long wareId;
    private Integer count;
}

