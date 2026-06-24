package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.cache.ProductCacheEvictor;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.repository.SpuInfoDescRepository;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service("spuInfoDescService")
public class SpuInfoDescServiceImpl implements SpuInfoDescService {

    @Autowired
    private SpuInfoDescRepository spuInfoDescRepository;
    @Autowired
    private ProductCacheEvictor productCacheEvictor;

    @Override
    @Transactional(readOnly = true)
    public SpuInfoDescEntity getById(Long id) {
        return spuInfoDescRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SpuInfoDescEntity entity) {
        spuInfoDescRepository.save(entity);
        if (entity != null) {
            // desc PK == spuId; editing the long description must refresh every SKU's detail cache.
            productCacheEvictor.evictItemsBySpuId(entity.getSpuId());
        }
    }

    @Override
    public void updateById(SpuInfoDescEntity entity) {
        spuInfoDescRepository.save(entity);
        if (entity != null) {
            productCacheEvictor.evictItemsBySpuId(entity.getSpuId());
        }
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        spuInfoDescRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuInfoDescEntity>().getPageable(params, Sort.by("id").ascending());
        Page<SpuInfoDescEntity> page = spuInfoDescRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveSpuInfoDesc(SpuInfoDescEntity descEntity) {
        spuInfoDescRepository.save(descEntity);
    }
}
