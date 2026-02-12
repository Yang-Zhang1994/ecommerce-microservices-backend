package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.entity.RefundInfoEntity;
import com.atguigu.gulimall.order.repository.RefundInfoRepository;
import com.atguigu.gulimall.order.service.RefundInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("refundInfoService")
public class RefundInfoServiceImpl implements RefundInfoService {

    @Autowired
    private RefundInfoRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<RefundInfoEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public RefundInfoEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(RefundInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(RefundInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
