package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * VO for batch saving sku images.
 */
@Data
public class SkuImagesSaveBatchVo {

    /**
     * Target skuId whose images will be replaced.
     */
    private Long skuId;

    /**
     * Image items for the sku.
     */
    private List<SkuImageItem> images;

    @Data
    public static class SkuImageItem {
        private String imgUrl;
        private Integer imgSort;
        private Integer defaultImg;
    }
}

