package com.atguigu.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU document for Elasticsearch and search API responses.
 * <p>Indexed fields: ids, {@code skuTitle}, {@code spuName}, {@code brandName}, {@code catalogName},
 * searchable/filterable SKU data, {@code attrs} as {@code attrId}+{@code attrValue} only.
 * Display fields ({@code brandImg}, {@code attrs[].attrName}) may still be refreshed from MySQL at read time.
 */
@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    /** SPU display name; indexed for keyword search. */
    private String spuName;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    /** Indexed for keyword search; may be refreshed from pms_brand at search time. */
    private String brandName;
    /** Response-only; sourced from pms_brand.logo at search time. */
    private String brandImg;
    /** Indexed for keyword search; may be refreshed from pms_category at search time. */
    private String catalogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;
        /** Response-only; sourced from pms_attr at search time. */
        private String attrName;
        private String attrValue;
    }
}
