package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.HomeSubjectSpuEntity;
import com.atguigu.gulimall.coupon.repository.HomeSubjectSpuRepository;
import com.atguigu.gulimall.coupon.service.HomeSubjectSpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("homeSubjectSpuService")
public class HomeSubjectSpuServiceImpl implements HomeSubjectSpuService {

    @Autowired
    private HomeSubjectSpuRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<HomeSubjectSpuEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public HomeSubjectSpuEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(HomeSubjectSpuEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(HomeSubjectSpuEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
