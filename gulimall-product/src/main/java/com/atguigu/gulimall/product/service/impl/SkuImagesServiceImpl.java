package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.repository.SkuImagesRepository;
import com.atguigu.gulimall.product.service.SkuImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("skuImagesService")
public class SkuImagesServiceImpl implements SkuImagesService {

    @Autowired
    private SkuImagesRepository skuImagesRepository;

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
