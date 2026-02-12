package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * sku信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_sku_info")
public class SkuInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private Long skuId;

    /** spuId */
    @Column(name = "spu_id")
    private Long spuId;

    /** sku名称 */
    @Column(name = "sku_name")
    private String skuName;

    /** sku介绍描述 */
    @Column(name = "sku_desc")
    private String skuDesc;

    /** 所属分类id */
    @Column(name = "catalog_id")
    private Long catalogId;

    /** 品牌id */
    @Column(name = "brand_id")
    private Long brandId;

    /** 默认图片 */
    @Column(name = "sku_default_img")
    private String skuDefaultImg;

    /** 标题 */
    @Column(name = "sku_title")
    private String skuTitle;

    /** 副标题 */
    @Column(name = "sku_subtitle")
    private String skuSubtitle;

    /** 价格 */
    @Column(name = "price")
    private BigDecimal price;

    /** 销量 */
    @Column(name = "sale_count")
    private Long saleCount;
}
