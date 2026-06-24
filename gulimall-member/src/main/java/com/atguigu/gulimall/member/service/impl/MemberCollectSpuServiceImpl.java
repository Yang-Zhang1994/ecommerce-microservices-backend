package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberCollectSpuEntity;
import com.atguigu.gulimall.member.repository.MemberCollectSpuRepository;
import com.atguigu.gulimall.member.service.MemberCollectSpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("memberCollectSpuService")
public class MemberCollectSpuServiceImpl implements MemberCollectSpuService {

    @Autowired
    private MemberCollectSpuRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberCollectSpuEntity>().getPageable(params, Sort.by("id").ascending());
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public MemberCollectSpuEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(MemberCollectSpuEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(MemberCollectSpuEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
