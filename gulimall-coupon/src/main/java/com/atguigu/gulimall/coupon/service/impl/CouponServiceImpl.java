package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.repository.CouponRepository;
import com.atguigu.gulimall.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("couponService")
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CouponEntity>().getPageable(params);
        Page<CouponEntity> page = couponRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public CouponEntity getById(Long id) {
        return couponRepository.findById(id).orElse(null);
    }

    @Override
    public void save(CouponEntity entity) {
        couponRepository.save(entity);
    }

    @Override
    public void updateById(CouponEntity entity) {
        couponRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        couponRepository.deleteAllById((Iterable<Long>) ids);
    }
}
