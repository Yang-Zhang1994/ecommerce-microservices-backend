package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.repository.MemberPriceRepository;
import com.atguigu.gulimall.coupon.repository.SkuFullReductionRepository;
import com.atguigu.gulimall.coupon.repository.SkuLadderRepository;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl implements SkuFullReductionService {

    @Autowired
    private SkuFullReductionRepository repository;
    @Autowired
    private SkuLadderRepository skuLadderRepository;
    @Autowired
    private MemberPriceRepository memberPriceRepository;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuFullReductionEntity>().getPageable(params);
        return new PageUtils(repository.findAll(pageable));
    }

    @Override
    public SkuFullReductionEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void save(SkuFullReductionEntity entity) {
        repository.save(entity);
    }

    @Override
    public void updateById(SkuFullReductionEntity entity) {
        repository.save(entity);
    }

    @Override
    public void removeByIds(Collection<?> ids) {
        repository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo to) {
        if (to == null || to.getSkuId() == null) {
            return;
        }
        Long skuId = to.getSkuId();

        // 1. SkuLadder (阶梯价格): fullCount, discount, countStatus
        if (to.getFullCount() > 0) {
            SkuLadderEntity ladder = new SkuLadderEntity();
            ladder.setSkuId(skuId);
            ladder.setFullCount(to.getFullCount());
            ladder.setDiscount(to.getDiscount());
            ladder.setAddOther(to.getCountStatus());
            skuLadderRepository.save(ladder);
        }

        // 2. SkuFullReduction (满减): fullPrice, reducePrice, priceStatus
        if (to.getFullPrice() != null && to.getFullPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            SkuFullReductionEntity reduction = new SkuFullReductionEntity();
            reduction.setSkuId(skuId);
            reduction.setFullPrice(to.getFullPrice());
            reduction.setReducePrice(to.getReducePrice() != null ? to.getReducePrice() : java.math.BigDecimal.ZERO);
            reduction.setAddOther(to.getPriceStatus());
            repository.save(reduction);
        }

        // 3. MemberPrice (会员价)
        List<MemberPrice> memberPrices = to.getMemberPrice() != null ? to.getMemberPrice() : Collections.emptyList();
        for (MemberPrice mp : memberPrices) {
            if (mp == null || mp.getPrice() == null) continue;
            MemberPriceEntity entity = new MemberPriceEntity();
            entity.setSkuId(skuId);
            entity.setMemberLevelId(mp.getId());
            entity.setMemberLevelName(mp.getName());
            entity.setMemberPrice(mp.getPrice());
            entity.setAddOther(0);
            memberPriceRepository.save(entity);
        }
    }
}
