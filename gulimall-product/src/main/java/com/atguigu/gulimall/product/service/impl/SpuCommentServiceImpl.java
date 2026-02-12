package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SpuCommentEntity;
import com.atguigu.gulimall.product.repository.SpuCommentRepository;
import com.atguigu.gulimall.product.service.SpuCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("spuCommentService")
public class SpuCommentServiceImpl implements SpuCommentService {

    @Autowired
    private SpuCommentRepository spuCommentRepository;

    @Override
    public SpuCommentEntity getById(Long id) {
        return spuCommentRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SpuCommentEntity entity) {
        spuCommentRepository.save(entity);
    }

    @Override
    public void updateById(SpuCommentEntity entity) {
        spuCommentRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        spuCommentRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuCommentEntity>().getPageable(params);
        Page<SpuCommentEntity> page = spuCommentRepository.findAll(pageable);
        return new PageUtils(page);
    }
}
