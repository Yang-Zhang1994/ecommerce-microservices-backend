package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.CouponSpuCategoryRelationEntity;
import com.atguigu.gulimall.coupon.repository.CouponSpuCategoryRelationRepository;
import com.atguigu.gulimall.coupon.service.CouponSpuCategoryRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("couponSpuCategoryRelationService")
public class CouponSpuCategoryRelationServiceImpl implements CouponSpuCategoryRelationService {

    @Autowired
    private CouponSpuCategoryRelationRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CouponSpuCategoryRelationEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public CouponSpuCategoryRelationEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(CouponSpuCategoryRelationEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(CouponSpuCategoryRelationEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
