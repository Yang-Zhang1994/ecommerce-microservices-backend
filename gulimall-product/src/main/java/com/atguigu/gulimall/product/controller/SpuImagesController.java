package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.service.ProductSearchIndexSyncService;
import com.atguigu.gulimall.product.service.SpuImagesService;
import com.atguigu.gulimall.product.vo.SpuImagesSaveBatchVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

import java.util.ArrayList;
import java.util.List;



/**
 * spu图片
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 19:20:59
 */
@RestController
@RequestMapping("product/spuimages")
public class SpuImagesController {
    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private ProductSearchIndexSyncService productSearchIndexSyncService;

    @RequestMapping("/byspu/{spuId}")
    public R listBySpuId(@PathVariable("spuId") Long spuId) {
        List<SpuImagesEntity> list = spuImagesService.listBySpuId(spuId);
        return R.ok().put("list", list);
    }

    @RequestMapping("/saveBatch")
    public R saveBatch(@RequestBody SpuImagesSaveBatchVo vo) {
        List<SpuImagesEntity> entities = new ArrayList<>();
        if (vo.getImages() != null) {
            for (int i = 0; i < vo.getImages().size(); i++) {
                SpuImagesSaveBatchVo.SpuImageItem item = vo.getImages().get(i);
                SpuImagesEntity e = new SpuImagesEntity();
                e.setImgUrl(item.getImgUrl());
                e.setImgSort(item.getImgSort() != null ? item.getImgSort() : i);
                e.setDefaultImg(item.getDefaultImg() != null ? item.getDefaultImg() : (i == 0 ? 1 : 0));
                entities.add(e);
            }
        }
        spuImagesService.saveBatchForSpu(vo.getSpuId(), entities);
        boolean searchSynced = productSearchIndexSyncService.refreshIfOnSale(vo.getSpuId());
        return ProductSearchIndexSyncService.okWithSearchSync(searchSynced);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuimages:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuImagesService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuimages:info")
    public R info(@PathVariable("id") Long id){
		SpuImagesEntity spuImages = spuImagesService.getById(id);

        return R.ok().put("spuImages", spuImages);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuimages:save")
    public R save(@RequestBody SpuImagesEntity spuImages){
		spuImagesService.save(spuImages);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuimages:update")
    public R update(@RequestBody SpuImagesEntity spuImages){
		spuImagesService.updateById(spuImages);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuimages:delete")
    public R delete(@RequestBody Long[] ids){
		spuImagesService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
