package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.repository.OrderItemRepository;
import com.atguigu.gulimall.order.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("orderItemService")
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderItemEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public OrderItemEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderItemEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(OrderItemEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
