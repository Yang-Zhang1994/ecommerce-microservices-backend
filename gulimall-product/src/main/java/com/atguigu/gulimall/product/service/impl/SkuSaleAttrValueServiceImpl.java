package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.repository.SkuSaleAttrValueRepository;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl implements SkuSaleAttrValueService {

    @Autowired
    private SkuSaleAttrValueRepository skuSaleAttrValueRepository;

    @Override
    public SkuSaleAttrValueEntity getById(Long id) {
        return skuSaleAttrValueRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SkuSaleAttrValueEntity entity) {
        skuSaleAttrValueRepository.save(entity);
    }

    @Override
    public void updateById(SkuSaleAttrValueEntity entity) {
        skuSaleAttrValueRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        skuSaleAttrValueRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public void saveAll(Iterable<SkuSaleAttrValueEntity> entities) {
        skuSaleAttrValueRepository.saveAll(entities);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuSaleAttrValueEntity>().getPageable(params);
        Page<SkuSaleAttrValueEntity> page = skuSaleAttrValueRepository.findAll(pageable);
        return new PageUtils(page);
    }
}
