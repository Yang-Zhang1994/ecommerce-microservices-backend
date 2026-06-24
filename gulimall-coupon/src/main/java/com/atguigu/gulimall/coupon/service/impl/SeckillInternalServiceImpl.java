package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.SeckillSessionVo;
import com.atguigu.common.to.SeckillSkuRelationVo;
import com.atguigu.common.to.SeckillWarmupSessionVo;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.repository.SeckillSessionRepository;
import com.atguigu.gulimall.coupon.repository.SeckillSkuRelationRepository;
import com.atguigu.gulimall.coupon.service.SeckillInternalService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SeckillInternalServiceImpl implements SeckillInternalService {

    private final SeckillSessionRepository sessionRepository;
    private final SeckillSkuRelationRepository skuRelationRepository;

    public SeckillInternalServiceImpl(SeckillSessionRepository sessionRepository,
                                      SeckillSkuRelationRepository skuRelationRepository) {
        this.sessionRepository = sessionRepository;
        this.skuRelationRepository = skuRelationRepository;
    }

    @Override
    public List<SeckillWarmupSessionVo> warmupData(int days) {
        Date now = new Date();
        Date rangeEnd = Date.from(LocalDate.now().plusDays(Math.max(1, days))
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<SeckillSessionEntity> sessions = sessionRepository
                .findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(1, rangeEnd, now);
        if (sessions == null || sessions.isEmpty()) {
            return List.of();
        }
        List<SeckillWarmupSessionVo> result = new ArrayList<>(sessions.size());
        for (SeckillSessionEntity session : sessions) {
            List<SeckillSkuRelationEntity> relations = skuRelationRepository
                    .findByPromotionSessionId(session.getId());
            if (relations == null || relations.isEmpty()) {
                continue;
            }
            SeckillWarmupSessionVo bundle = new SeckillWarmupSessionVo();
            bundle.setSession(toSessionVo(session));
            bundle.setSkus(relations.stream().map(this::toRelationVo).toList());
            result.add(bundle);
        }
        return result;
    }

    private SeckillSessionVo toSessionVo(SeckillSessionEntity entity) {
        SeckillSessionVo vo = new SeckillSessionVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    private SeckillSkuRelationVo toRelationVo(SeckillSkuRelationEntity entity) {
        SeckillSkuRelationVo vo = new SeckillSkuRelationVo();
        vo.setId(entity.getId());
        vo.setPromotionId(entity.getPromotionId());
        vo.setPromotionSessionId(entity.getPromotionSessionId());
        vo.setSkuId(entity.getSkuId());
        vo.setSeckillPrice(entity.getSeckillPrice());
        vo.setSeckillCount(entity.getSeckillCount());
        vo.setSeckillLimit(entity.getSeckillLimit());
        vo.setSeckillSort(entity.getSeckillSort());
        return vo;
    }
}
