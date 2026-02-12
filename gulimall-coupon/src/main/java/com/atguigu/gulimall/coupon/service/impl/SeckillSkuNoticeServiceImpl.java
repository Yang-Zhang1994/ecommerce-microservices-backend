package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SeckillSkuNoticeEntity;
import com.atguigu.gulimall.coupon.repository.SeckillSkuNoticeRepository;
import com.atguigu.gulimall.coupon.service.SeckillSkuNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("seckillSkuNoticeService")
public class SeckillSkuNoticeServiceImpl implements SeckillSkuNoticeService {

    @Autowired
    private SeckillSkuNoticeRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SeckillSkuNoticeEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SeckillSkuNoticeEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(SeckillSkuNoticeEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(SeckillSkuNoticeEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
