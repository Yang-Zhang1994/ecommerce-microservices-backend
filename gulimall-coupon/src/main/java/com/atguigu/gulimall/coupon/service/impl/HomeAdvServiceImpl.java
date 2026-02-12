package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.HomeAdvEntity;
import com.atguigu.gulimall.coupon.repository.HomeAdvRepository;
import com.atguigu.gulimall.coupon.service.HomeAdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("homeAdvService")
public class HomeAdvServiceImpl implements HomeAdvService {

    @Autowired
    private HomeAdvRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<HomeAdvEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public HomeAdvEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(HomeAdvEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(HomeAdvEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
