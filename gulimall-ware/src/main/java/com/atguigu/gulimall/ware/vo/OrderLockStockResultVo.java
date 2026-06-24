package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderLockStockResultVo {
    private Long taskId;
    private List<LockedStockVo> locked = new ArrayList<>();
}
