package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/** One sale attribute value and the SKU ids that use this value under the same SPU. */
@Data
public class AttrValueWithSkuIdVo {
    private String attrValue;
    private List<Long> skuIds;
}
