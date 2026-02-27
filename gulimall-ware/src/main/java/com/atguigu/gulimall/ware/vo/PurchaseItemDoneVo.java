package com.atguigu.gulimall.ware.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseItemDoneVo {
    /** purchase detail id (wms_purchase_detail.id), use this when known */
    private Long itemId;
    /** sku id (purchased product id), use this to resolve by purchaseId+skuId when itemId not provided */
    private Long skuId;
    @NotNull
    private Integer status;
    private String reason;
}
