package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.atguigu.gulimall.order.repository.OrderReturnReasonRepository;
import com.atguigu.gulimall.order.service.OrderReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("orderReturnReasonService")
public class OrderReturnReasonServiceImpl implements OrderReturnReasonService {

    @Autowired
    private OrderReturnReasonRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderReturnReasonEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public OrderReturnReasonEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderReturnReasonEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(OrderReturnReasonEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
