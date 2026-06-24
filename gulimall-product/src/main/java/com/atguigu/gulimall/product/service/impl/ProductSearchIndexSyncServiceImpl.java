package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.repository.SkuInfoRepository;
import com.atguigu.gulimall.product.service.ProductSearchIndexSyncService;
import com.atguigu.gulimall.product.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;

@Service
public class ProductSearchIndexSyncServiceImpl implements ProductSearchIndexSyncService {

    @Autowired
    private SpuInfoService spuInfoService;

    @Autowired
    private SkuInfoRepository skuInfoRepository;

    @Override
    public boolean refreshIfOnSale(Long spuId) {
        if (spuId == null) {
            return false;
        }
        return spuInfoService.refreshSearchIndexIfOnSale(spuId);
    }

    @Override
    public boolean refreshIfOnSaleBySkuId(Long skuId) {
        if (skuId == null) {
            return false;
        }
        return skuInfoRepository.findById(skuId)
                .map(SkuInfoEntity::getSpuId)
                .map(this::refreshIfOnSale)
                .orElse(false);
    }

    @Override
    public int refreshBySkuIds(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return 0;
        }
        LinkedHashSet<Long> distinct = new LinkedHashSet<>();
        for (Long skuId : skuIds) {
            if (skuId != null) {
                distinct.add(skuId);
            }
        }
        int synced = 0;
        for (Long skuId : distinct) {
            if (refreshIfOnSaleBySkuId(skuId)) {
                synced++;
            }
        }
        return synced;
    }
}
