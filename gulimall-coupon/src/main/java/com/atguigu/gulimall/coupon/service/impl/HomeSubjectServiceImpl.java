package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.HomeSubjectEntity;
import com.atguigu.gulimall.coupon.repository.HomeSubjectRepository;
import com.atguigu.gulimall.coupon.service.HomeSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("homeSubjectService")
public class HomeSubjectServiceImpl implements HomeSubjectService {

    @Autowired
    private HomeSubjectRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<HomeSubjectEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public HomeSubjectEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(HomeSubjectEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(HomeSubjectEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
