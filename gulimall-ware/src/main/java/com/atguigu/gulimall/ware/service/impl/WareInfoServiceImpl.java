package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.repository.WareInfoRepository;
import com.atguigu.gulimall.ware.service.WareInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service("wareInfoService")
public class WareInfoServiceImpl implements WareInfoService {

    @Autowired
    private WareInfoRepository repository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        Specification<WareInfoEntity> spec = (root, query, cb) -> {
            if (key == null || key.isEmpty()) return cb.conjunction();
            try {
                Long keyId = Long.parseLong(key);
                return cb.or(
                        cb.equal(root.get("id"), keyId),
                        cb.like(root.get("name"), "%" + key + "%"),
                        cb.like(root.get("address"), "%" + key + "%"),
                        cb.like(root.get("areacode"), "%" + key + "%")
                );
            } catch (NumberFormatException e) {
                return cb.or(
                        cb.like(root.get("name"), "%" + key + "%"),
                        cb.like(root.get("address"), "%" + key + "%"),
                        cb.like(root.get("areacode"), "%" + key + "%")
                );
            }
        };
        Pageable pageable = new Query<WareInfoEntity>().getPageable(params);
        Page<WareInfoEntity> page = repository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public WareInfoEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(WareInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(WareInfoEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }
}
