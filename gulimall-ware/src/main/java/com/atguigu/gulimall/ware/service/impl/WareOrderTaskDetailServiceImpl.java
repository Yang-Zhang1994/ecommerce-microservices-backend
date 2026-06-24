package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.repository.WareOrderTaskDetailRepository;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl implements WareOrderTaskDetailService {

    @Autowired
    private WareOrderTaskDetailRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<WareOrderTaskDetailEntity>().getPageable(params, Sort.by("id").ascending());
        Page<WareOrderTaskDetailEntity> page = repository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public WareOrderTaskDetailEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(WareOrderTaskDetailEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(WareOrderTaskDetailEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
