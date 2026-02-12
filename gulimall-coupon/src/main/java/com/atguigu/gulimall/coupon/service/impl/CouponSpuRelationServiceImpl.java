package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.CouponSpuRelationEntity;
import com.atguigu.gulimall.coupon.repository.CouponSpuRelationRepository;
import com.atguigu.gulimall.coupon.service.CouponSpuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("couponSpuRelationService")
public class CouponSpuRelationServiceImpl implements CouponSpuRelationService {

    @Autowired
    private CouponSpuRelationRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CouponSpuRelationEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public CouponSpuRelationEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(CouponSpuRelationEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(CouponSpuRelationEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
