package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.ProductSearchIndexSyncService;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.OrderSkuMetaVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * sku信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 19:20:59
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private ProductSearchIndexSyncService productSearchIndexSyncService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * Batch lightweight order meta by skuIds.
     * Gateway path: /api/product/skuinfo/order/meta
     */
    @RequestMapping("/order/meta")
    public R orderMeta(@RequestBody List<Long> skuIds){
        List<OrderSkuMetaVo> data = skuInfoService.getOrderSkuMetaBySkuIds(skuIds);
        return R.ok().put("data", data);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);
        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);
        boolean searchSynced = productSearchIndexSyncService.refreshIfOnSaleBySkuId(skuInfo.getSkuId());
        return ProductSearchIndexSyncService.okWithSearchSync(searchSynced);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok().put("msg",
                "SKU removed from catalog (images, sale attrs, stock rows, promotions). Order history is kept.");
    }

}
