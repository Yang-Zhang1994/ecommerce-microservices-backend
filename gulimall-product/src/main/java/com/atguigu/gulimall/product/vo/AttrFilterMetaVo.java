package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * Attribute metadata for mall search facet filtering (searchType=1 + allowed values).
 */
@Data
public class AttrFilterMetaVo {
    private Long attrId;
    private String attrName;
    /** Allowed option values from pms_attr.value_select (semicolon-separated in DB). */
    private List<String> allowedValues;
}
