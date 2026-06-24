package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/** SPU specification parameters grouped by attribute group. */
@Data
public class SpuItemAttrGroupVo {
    private Long groupId;
    private String groupName;
    private List<SpuItemAttrVo> attrs;
}
