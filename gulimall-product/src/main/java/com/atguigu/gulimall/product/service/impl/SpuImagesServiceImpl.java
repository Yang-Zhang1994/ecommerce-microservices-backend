package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.repository.SpuImagesRepository;
import com.atguigu.gulimall.product.service.SpuImagesService;
import com.atguigu.gulimall.product.vo.Images;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("spuImagesService")
public class SpuImagesServiceImpl implements SpuImagesService {

    @Autowired
    private SpuImagesRepository spuImagesRepository;

    @Override
    public SpuImagesEntity getById(Long id) {
        return spuImagesRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SpuImagesEntity entity) {
        spuImagesRepository.save(entity);
    }

    @Override
    public void updateById(SpuImagesEntity entity) {
        spuImagesRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        spuImagesRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public void saveAll(Iterable<SpuImagesEntity> entities) {
        spuImagesRepository.saveAll(entities);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuImagesEntity>().getPageable(params);
        Page<SpuImagesEntity> page = spuImagesRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveImages(Long id, List<Images> images) {
        if (images == null || images.isEmpty()) return;
        List<SpuImagesEntity> collect = images.stream().map(img -> {
            SpuImagesEntity e = new SpuImagesEntity();
            e.setSpuId(id);
            e.setImgUrl(img.getImgUrl());
            return e;
        }).toList();
        spuImagesRepository.saveAll(collect);
    }
}
