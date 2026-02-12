package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.repository.OrderRepository;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("orderService")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderEntity>().getPageable(params);
        Page<OrderEntity> page = orderRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public OrderEntity getById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderEntity entity) {
        orderRepository.save(entity);
    }

    @Override
    public void updateById(OrderEntity entity) {
        orderRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        orderRepository.deleteAllById((Iterable<Long>) ids);
    }
}
