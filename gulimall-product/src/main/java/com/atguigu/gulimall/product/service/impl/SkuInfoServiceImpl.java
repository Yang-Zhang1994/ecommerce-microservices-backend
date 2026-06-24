package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.cache.ProductCacheEvictor;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.repository.BrandRepository;
import com.atguigu.gulimall.product.repository.SkuInfoRepository;
import com.atguigu.gulimall.product.repository.SpuInfoRepository;
import com.atguigu.gulimall.product.service.ProductCascadeDeleteService;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.OrderSkuMetaVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("skuInfoService")
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuInfoRepository skuInfoRepository;
    @Autowired
    private SpuInfoRepository spuInfoRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ProductCascadeDeleteService productCascadeDeleteService;
    @Autowired
    private ProductCacheEvictor productCacheEvictor;

    @Override
    @Transactional(readOnly = true)
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
        if (entity != null) {
            productCacheEvictor.evictItemBySkuId(entity.getSkuId());
        }
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        List<Long> longIds = ids == null ? List.of() : ids.stream()
                .filter(java.util.Objects::nonNull)
                .map(v -> Long.parseLong(String.valueOf(v)))
                .toList();
        if (longIds.isEmpty()) {
            return;
        }
        productCascadeDeleteService.deleteSkus(longIds);
        longIds.forEach(productCacheEvictor::evictItemBySkuId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuInfoEntity>().getPageable(params, Sort.by("skuId").ascending());
        Page<SkuInfoEntity> page = skuInfoRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        skuInfoRepository.save(skuInfoEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        String spuId = (String) params.get("spuId");

        Specification<SkuInfoEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (spuId != null && !spuId.isEmpty() && !"0".equals(spuId)) {
                p = cb.and(p, cb.equal(root.get("spuId"), Long.parseLong(spuId)));
            }
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
        Pageable pageable = new Query<SkuInfoEntity>().getPageable(params, Sort.by("skuId").ascending());
        Page<SkuInfoEntity> page = skuInfoRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return skuInfoRepository.findBySpuId(spuId);
    }

    @Override
    public List<OrderSkuMetaVo> getOrderSkuMetaBySkuIds(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        Set<Long> uniq = skuIds.stream().filter(java.util.Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniq.isEmpty()) {
            return List.of();
        }
        List<SkuInfoEntity> skus = skuInfoRepository.findAllById(uniq);
        if (skus.isEmpty()) {
            return List.of();
        }

        Set<Long> spuIds = skus.stream().map(SkuInfoEntity::getSpuId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Set<Long> brandIds = skus.stream().map(SkuInfoEntity::getBrandId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SpuInfoEntity> spuMap = spuIds.isEmpty() ? Map.of() : spuInfoRepository.findAllById(spuIds).stream()
                .collect(Collectors.toMap(SpuInfoEntity::getId, Function.identity(), (a, b) -> a));
        Map<Long, BrandEntity> brandMap = brandIds.isEmpty() ? Map.of() : brandRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(BrandEntity::getBrandId, Function.identity(), (a, b) -> a));

        List<OrderSkuMetaVo> out = new ArrayList<>(skus.size());
        for (SkuInfoEntity sku : skus) {
            OrderSkuMetaVo vo = new OrderSkuMetaVo();
            vo.setSkuId(sku.getSkuId());
            vo.setSpuId(sku.getSpuId());
            vo.setCategoryId(sku.getCatalogId());
            vo.setSpuPic(sku.getSkuDefaultImg());
            SpuInfoEntity spu = sku.getSpuId() == null ? null : spuMap.get(sku.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getSpuName());
            }
            BrandEntity brand = sku.getBrandId() == null ? null : brandMap.get(sku.getBrandId());
            if (brand != null) {
                vo.setSpuBrand(brand.getName());
            }
            out.add(vo);
        }
        return out;
    }
}
