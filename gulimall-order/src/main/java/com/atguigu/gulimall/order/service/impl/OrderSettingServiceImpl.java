package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderSettingEntity;
import com.atguigu.gulimall.order.repository.OrderSettingRepository;
import com.atguigu.gulimall.order.service.OrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("orderSettingService")
public class OrderSettingServiceImpl implements OrderSettingService {

    @Autowired
    private OrderSettingRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderSettingEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public OrderSettingEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderSettingEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(OrderSettingEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
