package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * SPU row for admin list (includes resolved category / brand names).
 */
@Data
public class SpuInfoListVo {
    private Long id;
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private String catalogName;
    private String brandName;
    private BigDecimal weight;
    private Integer publishStatus;
    private Date createTime;
    private Date updateTime;
}
