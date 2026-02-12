package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.repository.SkuInfoRepository;
import com.atguigu.gulimall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Map;

@Service("skuInfoService")
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoRepository skuInfoRepository;

    @Override
    public SkuInfoEntity getById(Long id) {
        return skuInfoRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SkuInfoEntity entity) {
        skuInfoRepository.save(entity);
    }

    @Override
    public void updateById(SkuInfoEntity entity) {
        skuInfoRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        skuInfoRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuInfoEntity>().getPageable(params);
        Page<SkuInfoEntity> page = skuInfoRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        skuInfoRepository.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");

        Specification<SkuInfoEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (key != null && !key.isEmpty()) {
                try {
                    Long keyId = Long.parseLong(key);
                    p = cb.and(p, cb.or(
                            cb.equal(root.get("skuId"), keyId),
                            cb.like(root.get("skuName"), "%" + key + "%")
                    ));
                } catch (NumberFormatException e) {
                    p = cb.and(p, cb.like(root.get("skuName"), "%" + key + "%"));
                }
            }
            if (catelogId != null && !catelogId.isEmpty() && !"0".equals(catelogId)) {
                p = cb.and(p, cb.equal(root.get("catalogId"), Long.parseLong(catelogId)));
            }
            if (brandId != null && !brandId.isEmpty() && !"0".equals(brandId)) {
                p = cb.and(p, cb.equal(root.get("brandId"), Long.parseLong(brandId)));
            }
            if (min != null && !min.isEmpty() && new BigDecimal(min).compareTo(BigDecimal.ZERO) > 0) {
                p = cb.and(p, cb.ge(root.get("price"), new BigDecimal(min)));
            }
            if (max != null && !max.isEmpty()) {
                try {
                    BigDecimal maxVal = new BigDecimal(max);
                    if (maxVal.compareTo(BigDecimal.ZERO) > 0) {
                        p = cb.and(p, cb.le(root.get("price"), maxVal));
                    }
                } catch (Exception ignored) {}
            }
            return p;
        };
        Pageable pageable = new Query<SkuInfoEntity>().getPageable(params);
        Page<SkuInfoEntity> page = skuInfoRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }
}
