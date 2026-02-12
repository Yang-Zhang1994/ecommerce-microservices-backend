package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.CommentReplayEntity;
import com.atguigu.gulimall.product.repository.CommentReplayRepository;
import com.atguigu.gulimall.product.service.CommentReplayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("commentReplayService")
public class CommentReplayServiceImpl implements CommentReplayService {

    @Autowired
    private CommentReplayRepository commentReplayRepository;

    @Override
    public CommentReplayEntity getById(Long id) {
        return commentReplayRepository.findById(id).orElse(null);
    }

    @Override
    public void save(CommentReplayEntity entity) {
        commentReplayRepository.save(entity);
    }

    @Override
    public void updateById(CommentReplayEntity entity) {
        commentReplayRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        commentReplayRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CommentReplayEntity>().getPageable(params);
        Page<CommentReplayEntity> page = commentReplayRepository.findAll(pageable);
        return new PageUtils(page);
    }
}
