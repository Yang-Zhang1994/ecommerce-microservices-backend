package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.repository.SeckillSessionRepository;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.gulimall.coupon.support.SeckillWarmupNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("seckillSessionService")
public class SeckillSessionServiceImpl implements SeckillSessionService {

    @Autowired
    private SeckillSessionRepository repository;

    @Autowired
    private SeckillWarmupNotifier seckillWarmupNotifier;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SeckillSessionEntity>().getPageable(params, Sort.by("id").ascending());
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SeckillSessionEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SeckillSessionEntity entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(new java.util.Date());
        }
        repository.save(entity);
        seckillWarmupNotifier.notifyAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(SeckillSessionEntity entity) {
        repository.save(entity);
        seckillWarmupNotifier.notifyAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
        seckillWarmupNotifier.notifyAfterCommit();
    }
}
