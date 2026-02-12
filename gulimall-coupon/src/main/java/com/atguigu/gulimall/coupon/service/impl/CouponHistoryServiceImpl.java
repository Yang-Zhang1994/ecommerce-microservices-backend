package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.CouponHistoryEntity;
import com.atguigu.gulimall.coupon.repository.CouponHistoryRepository;
import com.atguigu.gulimall.coupon.service.CouponHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("couponHistoryService")
public class CouponHistoryServiceImpl implements CouponHistoryService {

    @Autowired
    private CouponHistoryRepository couponHistoryRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CouponHistoryEntity>().getPageable(params);
        Page<CouponHistoryEntity> page = couponHistoryRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public CouponHistoryEntity getById(Long id) {
        return couponHistoryRepository.findById(id).orElse(null);
    }

    @Override
    public void save(CouponHistoryEntity entity) {
        couponHistoryRepository.save(entity);
    }

    @Override
    public void updateById(CouponHistoryEntity entity) {
        couponHistoryRepository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        couponHistoryRepository.deleteAllById((Iterable<Long>) ids);
    }
}
