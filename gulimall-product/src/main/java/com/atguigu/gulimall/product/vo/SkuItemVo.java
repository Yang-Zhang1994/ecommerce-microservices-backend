package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * Aggregated VO for the product detail page: SKU, images, sale attrs, SPU description, spec groups.
 */
@Data
public class SkuItemVo {
    /** (1) SKU basic info — pms_sku_info */
    private SkuInfoEntity info;
    /** (2) SKU spec images — pms_sku_images (main gallery; changes when switching color/SKU) */
    private List<SkuImagesEntity> images;
    /** (2b) SPU shared promo gallery — pms_spu_images (shown below SKU gallery) */
    private List<SpuImagesEntity> spuImages;
    /** (3) SPU sale attributes (merged & deduped across SKUs of the same SPU) */
    private List<SkuItemSaleAttrVo> saleAttr;
    /** (4) SPU description — pms_spu_info_desc */
    private SpuInfoDescEntity desc;
    /** (5) SPU spec params (base attrs grouped by attribute group) */
    private List<SpuItemAttrGroupVo> groupAttrs;
}
