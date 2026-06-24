package com.atguigu.gulimall.search.controller;

import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Product listing: accept list of SkuEsModel and save to Elasticsearch. */
@Slf4j
@RestController
@RequestMapping("/search/product")
public class ProductSaveController {

    private final ProductSaveService productSaveService;

    public ProductSaveController(ProductSaveService productSaveService) {
        this.productSaveService = productSaveService;
    }

    @PostMapping("/up")
    public R productUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;
        try {
            b = productSaveService.productUp(skuEsModels);
        } catch (Exception e) {
            log.error("ProductSaveController product listing error: {}", e);
            return R.error(BisCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BisCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
        if (b) {
            return R.ok();
        } else {
            return R.error(BisCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BisCodeEnum.PRODUCT_UP_EXCEPTION.getMessage());
        }
    }

    /** Product off-shelf: remove all SKUs of the given SPU from Elasticsearch. */
    @PostMapping("/down/{spuId}")
    public R productDown(@PathVariable("spuId") Long spuId) {
        try {
            productSaveService.productDown(spuId);
            return R.ok();
        } catch (Exception e) {
            log.error("ProductSaveController product down error: spuId={}", spuId, e);
            return R.error("Product off-shelf failed");
        }
    }
}
