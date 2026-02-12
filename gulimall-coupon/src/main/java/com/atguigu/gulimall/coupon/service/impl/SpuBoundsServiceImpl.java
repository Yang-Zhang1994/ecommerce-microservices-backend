package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SpuBoundsEntity;
import com.atguigu.gulimall.coupon.repository.SpuBoundsRepository;
import com.atguigu.gulimall.coupon.service.SpuBoundsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("spuBoundsService")
public class SpuBoundsServiceImpl implements SpuBoundsService {

    @Autowired
    private SpuBoundsRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuBoundsEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SpuBoundsEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(SpuBoundsEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(SpuBoundsEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
