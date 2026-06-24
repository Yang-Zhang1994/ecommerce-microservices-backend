package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.repository.SkuLadderRepository;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("skuLadderService")
public class SkuLadderServiceImpl implements SkuLadderService {

    @Autowired
    private SkuLadderRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuLadderEntity>().getPageable(params, Sort.by("id").ascending());
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SkuLadderEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(SkuLadderEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(SkuLadderEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
