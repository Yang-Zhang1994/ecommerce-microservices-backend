package com.atguigu.gulimall.product.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.service.SkuItemService;
import com.atguigu.gulimall.product.validation.SkuIdValidator;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aggregated product detail API (gateway: /api/product/item/{skuId}).
 */
@RestController
@RequestMapping("product/item")
public class SkuItemController {

    @Autowired
    private SkuItemService skuItemService;
    @Autowired
    private SkuIdValidator skuIdValidator;

    @GetMapping("/{skuId}")
    public R item(@PathVariable("skuId") Long skuId) {
        // Cheap first gate: reject obviously out-of-range ids before any cache/DB lookup.
        if (!skuIdValidator.isPlausible(skuId)) {
            return R.error(404, "Product not found or unavailable");
        }
        SkuItemVo vo = skuItemService.item(skuId);
        if (vo == null) {
            return R.error(404, "Product not found or unavailable");
        }
        return R.ok().put("item", vo);
    }
}
