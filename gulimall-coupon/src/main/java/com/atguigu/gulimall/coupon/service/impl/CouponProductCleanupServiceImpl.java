package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.gulimall.coupon.repository.CouponSpuRelationRepository;
import com.atguigu.gulimall.coupon.repository.HomeSubjectSpuRepository;
import com.atguigu.gulimall.coupon.repository.MemberPriceRepository;
import com.atguigu.gulimall.coupon.repository.SeckillSkuNoticeRepository;
import com.atguigu.gulimall.coupon.repository.SeckillSkuRelationRepository;
import com.atguigu.gulimall.coupon.repository.SkuFullReductionRepository;
import com.atguigu.gulimall.coupon.repository.SkuLadderRepository;
import com.atguigu.gulimall.coupon.repository.SpuBoundsRepository;
import com.atguigu.gulimall.coupon.service.CouponProductCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CouponProductCleanupServiceImpl implements CouponProductCleanupService {

    @Autowired
    private SpuBoundsRepository spuBoundsRepository;
    @Autowired
    private CouponSpuRelationRepository couponSpuRelationRepository;
    @Autowired
    private HomeSubjectSpuRepository homeSubjectSpuRepository;
    @Autowired
    private SkuFullReductionRepository skuFullReductionRepository;
    @Autowired
    private SkuLadderRepository skuLadderRepository;
    @Autowired
    private MemberPriceRepository memberPriceRepository;
    @Autowired
    private SeckillSkuRelationRepository seckillSkuRelationRepository;
    @Autowired
    private SeckillSkuNoticeRepository seckillSkuNoticeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanupBySpuId(Long spuId) {
        if (spuId == null) {
            return;
        }
        spuBoundsRepository.deleteBySpuId(spuId);
        couponSpuRelationRepository.deleteBySpuId(spuId);
        homeSubjectSpuRepository.deleteBySpuId(spuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanupBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        skuFullReductionRepository.deleteBySkuIdIn(skuIds);
        skuLadderRepository.deleteBySkuIdIn(skuIds);
        memberPriceRepository.deleteBySkuIdIn(skuIds);
        seckillSkuRelationRepository.deleteBySkuIdIn(skuIds);
        seckillSkuNoticeRepository.deleteBySkuIdIn(skuIds);
    }
}
