package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.ProductSearchIndexSyncService;
import com.atguigu.gulimall.product.service.SpuInfoService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * spu信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 19:20:59
 */
@RestController
@RequestMapping("/product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    private ProductSearchIndexSyncService productSearchIndexSyncService;

    /** 探针：确认网关→product 可达，curl http://localhost:88/api/product/spuinfo/ping */
    @GetMapping("/ping")
    public R ping() {
        return R.ok().put("msg", "gulimall-product ok");
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo vo){
		spuInfoService.saveSpuInfo(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);
        boolean searchSynced = productSearchIndexSyncService.refreshIfOnSale(spuInfo.getId());
        return ProductSearchIndexSyncService.okWithSearchSync(searchSynced);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok().put("msg",
                "SPU removed from catalog (SKUs, images, attributes, stock rows, promotions). Order history is kept.");
    }

    /**
     * 商品上架（/product/spuinfo/{spuId}/up）。响应中返回当前 publishStatus 便于确认 DB 已更新。
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        boolean updated = spuInfoService.up(spuId);
        SpuInfoEntity spu = spuInfoService.getById(spuId);
        Integer publishStatus = spu != null ? spu.getPublishStatus() : null;
        return R.ok().put("publishStatus", publishStatus).put("updated", updated);
    }

    /**
     * 商品下架（/product/spuinfo/{spuId}/down）。从 ES 删除该 SPU 下所有 SKU，并更新 DB 为下架状态。
     */
    @PostMapping("/{spuId}/down")
    public R spuDown(@PathVariable("spuId") Long spuId) {
        boolean updated = spuInfoService.down(spuId);
        SpuInfoEntity spu = spuInfoService.getById(spuId);
        Integer publishStatus = spu != null ? spu.getPublishStatus() : null;
        return R.ok().put("publishStatus", publishStatus).put("updated", updated);
    }

}
