package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import com.atguigu.gulimall.order.repository.OrderReturnApplyRepository;
import com.atguigu.gulimall.order.service.OrderReturnApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("orderReturnApplyService")
public class OrderReturnApplyServiceImpl implements OrderReturnApplyService {

    @Autowired
    private OrderReturnApplyRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<OrderReturnApplyEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public OrderReturnApplyEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(OrderReturnApplyEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(OrderReturnApplyEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
