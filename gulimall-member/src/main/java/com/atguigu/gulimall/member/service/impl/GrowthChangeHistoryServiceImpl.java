package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.GrowthChangeHistoryEntity;
import com.atguigu.gulimall.member.repository.GrowthChangeHistoryRepository;
import com.atguigu.gulimall.member.service.GrowthChangeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("growthChangeHistoryService")
public class GrowthChangeHistoryServiceImpl implements GrowthChangeHistoryService {

    @Autowired
    private GrowthChangeHistoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<GrowthChangeHistoryEntity>().getPageable(params, Sort.by("id").ascending());
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public GrowthChangeHistoryEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(GrowthChangeHistoryEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(GrowthChangeHistoryEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
