package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.repository.SeckillSessionRepository;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("seckillSessionService")
public class SeckillSessionServiceImpl implements SeckillSessionService {

    @Autowired
    private SeckillSessionRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SeckillSessionEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SeckillSessionEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(SeckillSessionEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(SeckillSessionEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
