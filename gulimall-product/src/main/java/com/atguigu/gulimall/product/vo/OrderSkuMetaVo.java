package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * Lightweight SKU/SPU meta used by order-service when creating oms_order_item.
 */
@Data
public class OrderSkuMetaVo {
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String spuPic;
    private String spuBrand;
    private Long categoryId;
}

