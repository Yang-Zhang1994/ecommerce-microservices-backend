package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.repository.WareOrderTaskRepository;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("wareOrderTaskService")
public class WareOrderTaskServiceImpl implements WareOrderTaskService {

    @Autowired
    private WareOrderTaskRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<WareOrderTaskEntity>().getPageable(params);
        Page<WareOrderTaskEntity> page = repository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public WareOrderTaskEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(WareOrderTaskEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(WareOrderTaskEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
