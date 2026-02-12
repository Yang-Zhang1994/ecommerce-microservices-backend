package com.atguigu.gulimall.ware.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseDoneVo {
    // purchase order id
    @NotNull
    private Long id;
    // purchase order items / purchase detail items / purchase needs items
    private java.util.List<PurchaseItemDoneVo> items;
}
