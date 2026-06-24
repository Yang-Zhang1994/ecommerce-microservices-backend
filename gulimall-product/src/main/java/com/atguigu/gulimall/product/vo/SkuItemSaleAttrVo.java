package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/** One sale attribute on the SPU; each value lists which SKUs carry that value. */
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
