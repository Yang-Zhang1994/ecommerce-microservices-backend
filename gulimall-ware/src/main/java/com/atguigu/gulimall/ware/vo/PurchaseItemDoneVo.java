package com.atguigu.gulimall.ware.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseItemDoneVo {
    // purchase detail item id
    @NotNull
    private Long itemId;
    @NotNull
    private Integer status;
    private String reason;
}
