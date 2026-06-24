package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.cache.ProtectedCache;
import com.atguigu.gulimall.product.cache.ProductCacheKeys;
import com.atguigu.gulimall.product.config.MyThreadConfig;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.SkuImagesService;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.service.SkuItemService;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import com.atguigu.gulimall.product.service.SpuImagesService;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 商品详情聚合：(1) 先查 SKU 基本信息；随后 (2) 与 (3)(4)(5) 并行。
 */
@Service
public class SkuItemServiceImpl implements SkuItemService {

    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    @Qualifier(MyThreadConfig.PRODUCT_ITEM_EXECUTOR)
    private ThreadPoolExecutor productItemExecutor;

    @Autowired
    private ProtectedCache protectedCache;

    @Override
    public SkuItemVo item(Long skuId) {
        if (skuId == null) {
            return null;
        }
        // Cache-aside with penetration/breakdown/avalanche protection; loadItem only runs on a miss.
        return protectedCache.getWithProtection(
                ProductCacheKeys.item(skuId), SkuItemVo.class, () -> loadItem(skuId));
    }

    private SkuItemVo loadItem(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();

        SkuInfoEntity info = skuInfoService.getById(skuId);
        if (info == null) {
            return null;
        }
        skuItemVo.setInfo(info);
        Long spuId = info.getSpuId();
        Long catalogId = info.getCatalogId();

        ThreadPoolExecutor executor = productItemExecutor;

        CompletableFuture<List<SkuImagesEntity>> skuImageFuture =
                CompletableFuture.supplyAsync(() -> skuImagesService.getImagesBySkuId(skuId), executor);

        CompletableFuture<List<SpuImagesEntity>> spuImageFuture =
                CompletableFuture.supplyAsync(
                        () -> spuId != null ? spuImagesService.listBySpuId(spuId) : List.of(),
                        executor);

        CompletableFuture<List<SkuItemSaleAttrVo>> saleAttrFuture =
                CompletableFuture.supplyAsync(
                        () -> spuId != null ? skuSaleAttrValueService.getSaleAttrsBySpuId(spuId) : List.of(),
                        executor);

        CompletableFuture<SpuInfoDescEntity> descFuture =
                CompletableFuture.supplyAsync(
                        () -> spuId != null ? spuInfoDescService.getById(spuId) : null, executor);

        CompletableFuture<List<SpuItemAttrGroupVo>> groupAttrFuture =
                CompletableFuture.supplyAsync(
                        () ->
                                spuId != null
                                        ? attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId)
                                        : List.of(),
                        executor);

        CompletableFuture.allOf(skuImageFuture, spuImageFuture, saleAttrFuture, descFuture, groupAttrFuture).join();

        List<SkuImagesEntity> skuImages = sortSkuImages(skuImageFuture.join());
        List<SpuImagesEntity> spuImages = sortSpuImages(spuImageFuture.join());
        skuItemVo.setImages(skuImages);
        skuItemVo.setSpuImages(spuImages);
        applyDefaultImgFallback(info, skuImages, spuImages);

        List<SkuItemSaleAttrVo> sale = saleAttrFuture.join();
        skuItemVo.setSaleAttr(sale != null ? sale : List.of());
        skuItemVo.setDesc(descFuture.join());
        List<SpuItemAttrGroupVo> groups = groupAttrFuture.join();
        skuItemVo.setGroupAttrs(groups != null ? groups : List.of());

        return skuItemVo;
    }

    private List<SkuImagesEntity> sortSkuImages(List<SkuImagesEntity> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(img -> img.getImgUrl() != null && !img.getImgUrl().isBlank())
                .sorted(Comparator.comparing(SkuImagesEntity::getImgSort, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private List<SpuImagesEntity> sortSpuImages(List<SpuImagesEntity> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(img -> img.getImgUrl() != null && !img.getImgUrl().isBlank())
                .sorted(Comparator.comparing(SpuImagesEntity::getImgSort, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    /** Cart / list thumbnail: prefer SKU default, then first SKU image, then SPU default gallery image. */
    private void applyDefaultImgFallback(
            SkuInfoEntity info, List<SkuImagesEntity> skuImages, List<SpuImagesEntity> spuImages) {
        if (info == null) {
            return;
        }
        if (info.getSkuDefaultImg() != null && !info.getSkuDefaultImg().isBlank()) {
            return;
        }
        if (skuImages != null && !skuImages.isEmpty()) {
            String fromSku = skuImages.stream()
                    .filter(img -> Integer.valueOf(1).equals(img.getDefaultImg()))
                    .map(SkuImagesEntity::getImgUrl)
                    .findFirst()
                    .orElse(skuImages.get(0).getImgUrl());
            info.setSkuDefaultImg(fromSku);
            return;
        }
        if (spuImages != null && !spuImages.isEmpty()) {
            String fromSpu = spuImages.stream()
                    .filter(img -> Integer.valueOf(1).equals(img.getDefaultImg()))
                    .map(SpuImagesEntity::getImgUrl)
                    .findFirst()
                    .orElse(spuImages.get(0).getImgUrl());
            info.setSkuDefaultImg(fromSpu);
        }
    }
}
