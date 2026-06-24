package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpuImagesSaveBatchVo {

    private Long spuId;
    private List<SpuImageItem> images;

    @Data
    public static class SpuImageItem {
        private String imgUrl;
        private Integer imgSort;
        private Integer defaultImg;
    }
}
