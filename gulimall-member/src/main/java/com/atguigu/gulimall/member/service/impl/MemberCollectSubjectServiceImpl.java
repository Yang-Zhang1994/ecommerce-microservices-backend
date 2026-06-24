package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberCollectSubjectEntity;
import com.atguigu.gulimall.member.repository.MemberCollectSubjectRepository;
import com.atguigu.gulimall.member.service.MemberCollectSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("memberCollectSubjectService")
public class MemberCollectSubjectServiceImpl implements MemberCollectSubjectService {

    @Autowired
    private MemberCollectSubjectRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<MemberCollectSubjectEntity>().getPageable(params, Sort.by("id").ascending());
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public MemberCollectSubjectEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(MemberCollectSubjectEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(MemberCollectSubjectEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
