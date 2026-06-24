package com.atguigu.gulimall.product.vo;

import lombok.Data;

/** One spec row: attribute name and value (detail page parameter table). */
@Data
public class SpuItemAttrVo {
    private Long attrId;
    private String attrName;
    private String attrValue;
}
