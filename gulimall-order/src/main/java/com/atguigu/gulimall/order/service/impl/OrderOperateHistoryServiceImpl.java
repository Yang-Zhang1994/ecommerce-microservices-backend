package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderOperateHistoryEntity;
import com.atguigu.gulimall.order.repository.OrderOperateHistoryRepository;
import com.atguigu.gulimall.order.service.OrderOperateHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("orderOperateHistoryService")
public class OrderOperateHistoryServiceImpl implements OrderOperateHistoryService {

    @Autowired
    private OrderOperateHistoryRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderOperateHistoryEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public OrderOperateHistoryEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderOperateHistoryEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(OrderOperateHistoryEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
