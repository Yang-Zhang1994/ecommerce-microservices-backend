package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.repository.SpuInfoDescRepository;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("spuInfoDescService")
public class SpuInfoDescServiceImpl implements SpuInfoDescService {

    @Autowired
    private SpuInfoDescRepository spuInfoDescRepository;

    @Override
    public SpuInfoDescEntity getById(Long id) {
        return spuInfoDescRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SpuInfoDescEntity entity) {
        spuInfoDescRepository.save(entity);
    }

    @Override
    public void updateById(SpuInfoDescEntity entity) {
        spuInfoDescRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        spuInfoDescRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuInfoDescEntity>().getPageable(params);
        Page<SpuInfoDescEntity> page = spuInfoDescRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfoDesc(SpuInfoDescEntity descEntity) {
        spuInfoDescRepository.save(descEntity);
    }
}
