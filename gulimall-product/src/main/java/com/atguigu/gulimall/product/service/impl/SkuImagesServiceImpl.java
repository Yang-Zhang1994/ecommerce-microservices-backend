package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.repository.SkuImagesRepository;
import com.atguigu.gulimall.product.service.SkuImagesService;
import com.atguigu.gulimall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("skuImagesService")
public class SkuImagesServiceImpl implements SkuImagesService {

    @Autowired
    private SkuImagesRepository skuImagesRepository;

    @Autowired
    private SkuInfoService skuInfoService;

    @Override
    public List<SkuImagesEntity> listBySkuId(Long skuId) {
        return skuImagesRepository.findBySkuId(skuId);
    }

    @Override
    @Transactional
    public void saveBatchForSku(Long skuId, List<SkuImagesEntity> images) {
        List<SkuImagesEntity> existing = skuImagesRepository.findBySkuId(skuId);
        if (!existing.isEmpty()) {
            skuImagesRepository.deleteAll(existing);
        }
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                SkuImagesEntity e = images.get(i);
                e.setSkuId(skuId);
                e.setImgSort(i);
                if (e.getDefaultImg() == null) {
                    e.setDefaultImg(i == 0 ? 1 : 0);
                }
            }
            skuImagesRepository.saveAll(images);
            String defaultImgUrl = images.stream()
                    .filter(img -> Integer.valueOf(1).equals(img.getDefaultImg()))
                    .map(SkuImagesEntity::getImgUrl)
                    .findFirst()
                    .orElse(images.get(0).getImgUrl());
            SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
            if (skuInfo != null) {
                skuInfo.setSkuDefaultImg(defaultImgUrl);
                skuInfoService.updateById(skuInfo);
            }
        } else {
            SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
            if (skuInfo != null) {
                skuInfo.setSkuDefaultImg(null);
                skuInfoService.updateById(skuInfo);
            }
        }
    }

    @Override
    public SkuImagesEntity getById(Long id) {
        return skuImagesRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SkuImagesEntity entity) {
        skuImagesRepository.save(entity);
    }

    @Override
    public void updateById(SkuImagesEntity entity) {
        skuImagesRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        skuImagesRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public void saveAll(Iterable<SkuImagesEntity> entities) {
        skuImagesRepository.saveAll(entities);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuImagesEntity>().getPageable(params);
        Page<SkuImagesEntity> page = skuImagesRepository.findAll(pageable);
        return new PageUtils(page);
    }
}
