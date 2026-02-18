package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.repository.MemberLevelRepository;
import com.atguigu.gulimall.member.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service("memberLevelService")
public class MemberLevelServiceImpl implements MemberLevelService {

    @Autowired
    private MemberLevelRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        Pageable pageable = new Query<MemberLevelEntity>().getPageable(params);

        if (key == null || key.trim().isEmpty()) {
            return new PageUtils(repository.findAll(pageable));
        }

        String pattern = "%" + key.trim().toLowerCase() + "%";
        Specification<MemberLevelEntity> spec = (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("note")), pattern)
                );
        return new PageUtils(repository.findAll(spec, pageable));
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
