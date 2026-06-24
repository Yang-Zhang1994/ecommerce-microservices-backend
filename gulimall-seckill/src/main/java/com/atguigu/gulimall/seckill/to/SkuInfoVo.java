package com.atguigu.gulimall.seckill.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Minimal SKU info pulled from gulimall-product for seckill display.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkuInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Long spuId;
    private String skuName;
    private String skuTitle;
    private String skuSubtitle;
    private String skuDefaultImg;
    private BigDecimal price;
}
