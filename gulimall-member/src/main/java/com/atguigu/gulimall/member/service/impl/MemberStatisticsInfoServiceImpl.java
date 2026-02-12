package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberStatisticsInfoEntity;
import com.atguigu.gulimall.member.repository.MemberStatisticsInfoRepository;
import com.atguigu.gulimall.member.service.MemberStatisticsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("memberStatisticsInfoService")
public class MemberStatisticsInfoServiceImpl implements MemberStatisticsInfoService {

    @Autowired
    private MemberStatisticsInfoRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberStatisticsInfoEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public MemberStatisticsInfoEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(MemberStatisticsInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(MemberStatisticsInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
