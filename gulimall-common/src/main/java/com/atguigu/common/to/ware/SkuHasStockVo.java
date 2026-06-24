package com.atguigu.common.to.ware;

import lombok.Data;

/**
 * 记录对应 sku 是否有库存，供 ware 与 product 等模块共用
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
