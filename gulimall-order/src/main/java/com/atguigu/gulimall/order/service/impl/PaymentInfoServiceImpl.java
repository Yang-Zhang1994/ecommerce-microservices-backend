package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.repository.PaymentInfoRepository;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("paymentInfoService")
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    private PaymentInfoRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<PaymentInfoEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public PaymentInfoEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(PaymentInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(PaymentInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
