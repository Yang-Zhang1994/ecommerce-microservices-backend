package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.repository.MemberLevelRepository;
import com.atguigu.gulimall.member.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("memberLevelService")
public class MemberLevelServiceImpl implements MemberLevelService {

    @Autowired
    private MemberLevelRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberLevelEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public MemberLevelEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(MemberLevelEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(MemberLevelEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
