package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.repository.SeckillSkuRelationRepository;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.atguigu.gulimall.coupon.support.SeckillWarmupNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl implements SeckillSkuRelationService {

    @Autowired
    private SeckillSkuRelationRepository repository;

    @Autowired
    private SeckillWarmupNotifier seckillWarmupNotifier;

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SeckillSkuRelationEntity>().getPageable(params, Sort.by("id").ascending());
        Long sessionId = parseSessionId(params == null ? null : params.get("promotionSessionId"));
        if (sessionId != null) {
            return new PageUtils(repository.findByPromotionSessionId(sessionId, pageable));
        }
        return new PageUtils(repository.findAll(pageable));
    }

    private static Long parseSessionId(Object raw) {
        if (raw == null) {
            return null;
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty() || "0".equals(text)) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public SeckillSkuRelationEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SeckillSkuRelationEntity entity) {
        repository.save(entity);
        seckillWarmupNotifier.notifyAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(SeckillSkuRelationEntity entity) {
        repository.save(entity);
        seckillWarmupNotifier.notifyAfterCommit();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
        seckillWarmupNotifier.notifyAfterCommit();
    }
}
