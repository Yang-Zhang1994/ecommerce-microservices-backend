package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.client.CouponApi;
import com.atguigu.common.client.OrderApi;
import com.atguigu.common.client.SearchApi;
import com.atguigu.common.client.WareApi;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.exception.BisException;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.repository.ProductAttrValueRepository;
import com.atguigu.gulimall.product.repository.SkuImagesRepository;
import com.atguigu.gulimall.product.repository.SkuInfoRepository;
import com.atguigu.gulimall.product.repository.SkuSaleAttrValueRepository;
import com.atguigu.gulimall.product.repository.SpuCommentRepository;
import com.atguigu.gulimall.product.repository.SpuImagesRepository;
import com.atguigu.gulimall.product.repository.SpuInfoDescRepository;
import com.atguigu.gulimall.product.repository.SpuInfoRepository;
import com.atguigu.gulimall.product.service.ProductCascadeDeleteService;
import com.atguigu.gulimall.product.service.SpuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductCascadeDeleteServiceImpl implements ProductCascadeDeleteService {

    @Autowired
    private SkuInfoRepository skuInfoRepository;
    @Autowired
    private SpuInfoRepository spuInfoRepository;
    @Autowired
    private SpuInfoDescRepository spuInfoDescRepository;
    @Autowired
    private SpuImagesRepository spuImagesRepository;
    @Autowired
    private ProductAttrValueRepository productAttrValueRepository;
    @Autowired
    private SkuImagesRepository skuImagesRepository;
    @Autowired
    private SkuSaleAttrValueRepository skuSaleAttrValueRepository;
    @Autowired
    private SpuCommentRepository spuCommentRepository;
    @Lazy
    @Autowired
    private SpuInfoService spuInfoService;
    @Autowired
    private OrderApi orderApi;
    @Autowired
    private WareApi wareApi;
    @Autowired
    private CouponApi couponApi;
    @Autowired
    private SearchApi searchApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkus(Collection<Long> skuIds) {
        List<Long> ids = normalizeIds(skuIds);
        if (ids.isEmpty()) {
            return;
        }
        Set<Long> spuIds = collectSpuIds(ids);
        purgeSkus(ids, spuIds, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpus(Collection<Long> spuIds) {
        List<Long> ids = normalizeIds(spuIds);
        if (ids.isEmpty()) {
            return;
        }
        List<Long> allSkuIds = new ArrayList<>();
        for (Long spuId : ids) {
            skuInfoRepository.findBySpuId(spuId).stream()
                    .map(SkuInfoEntity::getSkuId)
                    .forEach(allSkuIds::add);
        }
        if (!allSkuIds.isEmpty()) {
            purgeSkus(allSkuIds, new HashSet<>(ids), false);
        } else {
            removeFromSearch(new HashSet<>(ids));
        }
        for (Long spuId : ids) {
            invokeCouponSpuCleanup(spuId);
            deleteSpuLocal(spuId);
        }
    }

    /**
     * @param refreshSearchAfter when true and parent SPU remains on sale, re-index remaining SKUs
     */
    private void purgeSkus(List<Long> skuIds, Set<Long> spuIds, boolean refreshSearchAfter) {
        assertDeletable(skuIds);
        removeFromSearch(spuIds);
        invokeWareDelete(skuIds);
        invokeCouponSkuCleanup(skuIds);
        for (Long skuId : skuIds) {
            deleteSkuLocal(skuId);
        }
        if (refreshSearchAfter) {
            refreshSearchForSpus(spuIds);
        }
    }

    private void assertDeletable(List<Long> skuIds) {
        List<Long> orderBlocked = fetchActiveOrderSkus(skuIds);
        if (!orderBlocked.isEmpty()) {
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_ACTIVE_ORDERS);
        }
        List<Long> stockBlocked = fetchLockedSkus(skuIds);
        if (!stockBlocked.isEmpty()) {
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_LOCKED_STOCK);
        }
    }

    private List<Long> fetchActiveOrderSkus(List<Long> skuIds) {
        try {
            R r = orderApi.activeOrderSkus(skuIds);
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                throw new BisException(BisCodeEnum.PRODUCT_DELETE_SERVICE_UNAVAILABLE);
            }
            return longListFromData(r.get("data"));
        } catch (BisException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Order delete guard failed: {}", e.toString());
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_SERVICE_UNAVAILABLE);
        }
    }

    private List<Long> fetchLockedSkus(List<Long> skuIds) {
        try {
            R r = wareApi.lockedSkus(skuIds);
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                throw new BisException(BisCodeEnum.PRODUCT_DELETE_SERVICE_UNAVAILABLE);
            }
            return longListFromData(r.get("data"));
        } catch (BisException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Ware locked-stock guard failed: {}", e.toString());
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_SERVICE_UNAVAILABLE);
        }
    }

    private void deleteSkuLocal(Long skuId) {
        spuCommentRepository.deleteBySkuIdIn(List.of(skuId));
        skuImagesRepository.deleteBySkuId(skuId);
        skuSaleAttrValueRepository.deleteBySkuId(skuId);
        skuInfoRepository.deleteById(skuId);
    }

    private void deleteSpuLocal(Long spuId) {
        spuCommentRepository.deleteBySpuId(spuId);
        spuImagesRepository.deleteBySpuId(spuId);
        productAttrValueRepository.deleteBySpuId(spuId);
        if (spuInfoDescRepository.existsById(spuId)) {
            spuInfoDescRepository.deleteById(spuId);
        }
        spuInfoRepository.deleteById(spuId);
    }

    private void removeFromSearch(Set<Long> spuIds) {
        for (Long spuId : spuIds) {
            if (spuId == null) {
                continue;
            }
            try {
                spuInfoService.down(spuId);
            } catch (Exception e) {
                log.warn("Search/DB down before delete failed for spuId={}: {}", spuId, e.toString());
                try {
                    searchApi.productDown(spuId);
                } catch (Exception ignored) {
                    // best effort
                }
            }
        }
    }

    private void refreshSearchForSpus(Set<Long> spuIds) {
        for (Long spuId : spuIds) {
            if (spuId == null) {
                continue;
            }
            SpuInfoEntity spu = spuInfoRepository.findById(spuId).orElse(null);
            if (spu == null
                    || spu.getPublishStatus() == null
                    || spu.getPublishStatus() != ProductConstant.StatusEnum.SPU_UP.getCode()) {
                continue;
            }
            List<SkuInfoEntity> remaining = skuInfoRepository.findBySpuId(spuId);
            if (remaining.isEmpty()) {
                spuInfoService.down(spuId);
            } else {
                spuInfoService.up(spuId);
            }
        }
    }

    private void invokeWareDelete(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        try {
            R r = wareApi.deleteBySkuIds(skuIds);
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                log.error("Ware delete-by-sku-ids returned non-ok: {}", r);
                throw new BisException(BisCodeEnum.PRODUCT_DELETE_REMOTE_CLEANUP_FAILED);
            }
        } catch (BisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ware delete-by-sku-ids failed", e);
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_REMOTE_CLEANUP_FAILED);
        }
    }

    private void invokeCouponSkuCleanup(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        try {
            R r = couponApi.cleanupSkus(skuIds);
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                log.error("Coupon sku cleanup returned non-ok: {}", r);
                throw new BisException(BisCodeEnum.PRODUCT_DELETE_REMOTE_CLEANUP_FAILED);
            }
        } catch (BisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Coupon sku cleanup failed", e);
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_REMOTE_CLEANUP_FAILED);
        }
    }

    private void invokeCouponSpuCleanup(Long spuId) {
        try {
            R r = couponApi.cleanupSpu(spuId);
            if (r == null || r.getCode() == null || r.getCode() != 0) {
                log.error("Coupon spu cleanup returned non-ok: {}", r);
                throw new BisException(BisCodeEnum.PRODUCT_DELETE_REMOTE_CLEANUP_FAILED);
            }
        } catch (BisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Coupon spu cleanup failed for spuId={}", spuId, e);
            throw new BisException(BisCodeEnum.PRODUCT_DELETE_REMOTE_CLEANUP_FAILED);
        }
    }

    private Set<Long> collectSpuIds(List<Long> skuIds) {
        Set<Long> spuIds = new HashSet<>();
        for (Long skuId : skuIds) {
            skuInfoRepository.findById(skuId).map(SkuInfoEntity::getSpuId).ifPresent(spuIds::add);
        }
        return spuIds;
    }

    private static List<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static List<Long> longListFromData(Object data) {
        if (data == null) {
            return List.of();
        }
        if (data instanceof List<?> list) {
            List<Long> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Number n) {
                    out.add(n.longValue());
                } else if (o != null) {
                    try {
                        out.add(Long.parseLong(String.valueOf(o)));
                    } catch (NumberFormatException ignored) {
                        // skip
                    }
                }
            }
            return out;
        }
        return List.of();
    }
}
