package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.repository.MemberPriceRepository;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("memberPriceService")
public class MemberPriceServiceImpl implements MemberPriceService {

    @Autowired
    private MemberPriceRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberPriceEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public MemberPriceEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(MemberPriceEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(MemberPriceEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
