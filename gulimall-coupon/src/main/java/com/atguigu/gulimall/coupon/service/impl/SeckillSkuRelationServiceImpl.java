package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.repository.SeckillSkuRelationRepository;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl implements SeckillSkuRelationService {

    @Autowired
    private SeckillSkuRelationRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SeckillSkuRelationEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SeckillSkuRelationEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(SeckillSkuRelationEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(SeckillSkuRelationEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
