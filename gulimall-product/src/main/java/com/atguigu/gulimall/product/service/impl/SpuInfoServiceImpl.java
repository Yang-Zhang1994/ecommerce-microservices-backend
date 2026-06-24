package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.client.SearchApi;
import com.atguigu.common.client.WareApi;
import com.atguigu.common.to.ware.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.cache.ProductCacheEvictor;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.common.client.CouponApi;
import com.atguigu.gulimall.product.repository.CategoryRepository;
import com.atguigu.gulimall.product.repository.SpuInfoRepository;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("spuInfoService")
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SpuInfoRepository spuInfoRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService imageService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImageService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponApi couponApi;
    @Autowired
    private ProductCacheEvictor productCacheEvictor;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SearchApi searchApi;
    @Autowired
    private WareApi wareApi;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductCascadeDeleteService productCascadeDeleteService;

    @Override
    public SpuInfoEntity getById(Long id) {
        return spuInfoRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SpuInfoEntity entity) {
        spuInfoRepository.save(entity);
    }

    @Override
    public void updateById(SpuInfoEntity entity) {
        if (entity.getId() == null) {
            spuInfoRepository.save(entity);
            return;
        }
        SpuInfoEntity existing = spuInfoRepository.findById(entity.getId())
                .orElseThrow(() -> new IllegalArgumentException("SPU not found: " + entity.getId()));
        if (entity.getSpuName() != null) {
            existing.setSpuName(entity.getSpuName());
        }
        if (entity.getSpuDescription() != null) {
            existing.setSpuDescription(entity.getSpuDescription());
        }
        if (entity.getCatalogId() != null) {
            existing.setCatalogId(entity.getCatalogId());
        }
        if (entity.getBrandId() != null) {
            existing.setBrandId(entity.getBrandId());
        }
        if (entity.getWeight() != null) {
            existing.setWeight(entity.getWeight());
        }
        if (entity.getPublishStatus() != null) {
            existing.setPublishStatus(entity.getPublishStatus());
        }
        if (entity.getCreateTime() != null) {
            existing.setCreateTime(entity.getCreateTime());
        }
        existing.setUpdateTime(new Date());
        spuInfoRepository.save(existing);
        productCacheEvictor.evictItemsBySpuId(existing.getId());
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<Long> spuIds = ids.stream()
                .filter(java.util.Objects::nonNull)
                .map(v -> Long.parseLong(String.valueOf(v)))
                .toList();
        productCascadeDeleteService.deleteSpus(spuIds);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuInfoEntity>().getPageable(params, Sort.by("id").ascending());
        Page<SpuInfoEntity> page = spuInfoRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        saveBaseSpuInfo(spuInfoEntity);

        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", vo.getDecript()));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        imageService.saveImages(spuInfoEntity.getId(), vo.getImages());

        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> list = baseAttrs.stream().map(b -> {
            ProductAttrValueEntity entity = new ProductAttrValueEntity();
            entity.setAttrId(b.getAttrId());
            AttrEntity attrEntity = attrService.getById(b.getAttrId());
            entity.setAttrName(attrEntity != null ? attrEntity.getAttrName() : null);
            entity.setAttrValue(b.getAttrValues());
            entity.setQuickShow(b.getShowDesc());
            entity.setSpuId(spuInfoEntity.getId());
            return entity;
        }).toList();
        productAttrValueService.saveProductAttr(list);

        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(vo.getBounds(), spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponApi.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            // log remote failure but continue
        }

        List<Skus> skus = vo.getSkus();
        if (skus != null && !skus.isEmpty()) {
            skus.forEach(sku -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setPrice(new BigDecimal(sku.getPrice()));
                List<String> descar = sku.getDescar();
                if (descar != null && !descar.isEmpty()) {
                    skuInfoEntity.setSkuDesc(String.join(" ", descar));
                }
                String defaultImg = "";
                for (Images img : sku.getImages()) {
                    if (img.getDefaultImg() == 1) defaultImg = img.getImgUrl();
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = sku.getImages().stream()
                        .map(img -> {
                            SkuImagesEntity e = new SkuImagesEntity();
                            e.setSkuId(skuId);
                            e.setImgUrl(img.getImgUrl());
                            e.setDefaultImg(img.getDefaultImg());
                            return e;
                        })
                        .filter(e -> e.getImgUrl() != null && !e.getImgUrl().isEmpty())
                        .toList();
                skuImageService.saveAll(imagesEntities);

                List<Attr> saleAttrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrs.stream().map(a -> {
                    SkuSaleAttrValueEntity e = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, e);
                    e.setSkuId(skuId);
                    return e;
                }).toList();
                skuSaleAttrValueService.saveAll(saleAttrValueEntities);

                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                    R r1 = couponApi.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        // log remote failure but continue
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        spuInfoRepository.save(spuInfoEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String brandId = (String) params.get("brandId");
        String catelogId = (String) params.get("catelogId");

        Specification<SpuInfoEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (key != null && !key.isEmpty()) {
                p = cb.and(p, spuKeyPredicate(cb, root, key));
            }
            if (status != null && !status.isEmpty()) {
                p = cb.and(p, cb.equal(root.get("publishStatus"), Integer.parseInt(status)));
            }
            if (brandId != null && !brandId.isEmpty() && !"0".equals(brandId)) {
                p = cb.and(p, cb.equal(root.get("brandId"), Long.parseLong(brandId)));
            }
            if (catelogId != null && !catelogId.isEmpty() && !"0".equals(catelogId)) {
                p = cb.and(p, cb.equal(root.get("catalogId"), Long.parseLong(catelogId)));
            }
            return p;
        };
        Pageable pageable = new Query<SpuInfoEntity>().getPageable(params, Sort.by("id").ascending());
        Page<SpuInfoEntity> page = spuInfoRepository.findAll(spec, pageable);
        PageUtils pageUtils = new PageUtils(page);
        List<SpuInfoEntity> records = page.getContent();
        if (records.isEmpty()) {
            return pageUtils;
        }
        List<Long> brandIdsList = records.stream().map(SpuInfoEntity::getBrandId).filter(Objects::nonNull).distinct().toList();
        List<Long> catalogIdsList = records.stream().map(SpuInfoEntity::getCatalogId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> brandNameMap = brandService.getBrandByIds(brandIdsList).stream()
                .collect(Collectors.toMap(BrandEntity::getBrandId, b -> b.getName() == null ? "" : b.getName().trim(), (a, b) -> a));
        Map<Long, String> catalogNameMap = categoryRepository.findAllById(catalogIdsList).stream()
                .collect(Collectors.toMap(CategoryEntity::getCatId, c -> c.getName() == null ? "" : c.getName().trim(), (a, b) -> a));
        List<SpuInfoListVo> list = records.stream().map(spu -> {
            SpuInfoListVo vo = new SpuInfoListVo();
            BeanUtils.copyProperties(spu, vo);
            if (spu.getBrandId() != null) {
                vo.setBrandName(brandNameMap.getOrDefault(spu.getBrandId(), String.valueOf(spu.getBrandId())));
            }
            if (spu.getCatalogId() != null) {
                vo.setCatalogName(catalogNameMap.getOrDefault(spu.getCatalogId(), String.valueOf(spu.getCatalogId())));
            }
            return vo;
        }).collect(Collectors.toList());
        pageUtils.setList(list);
        return pageUtils;
    }

    private Predicate spuKeyPredicate(CriteriaBuilder cb, Root<SpuInfoEntity> root, String key) {
        String trimmed = key.trim();
        String pattern = "%" + trimmed.toLowerCase(Locale.ROOT) + "%";
        try {
            Long keyId = Long.parseLong(trimmed);
            return cb.or(
                    cb.equal(root.get("id"), keyId),
                    cb.like(cb.lower(root.get("spuName")), pattern),
                    cb.like(cb.lower(root.get("spuDescription")), pattern)
            );
        } catch (NumberFormatException e) {
            return cb.or(
                    cb.like(cb.lower(root.get("spuName")), pattern),
                    cb.like(cb.lower(root.get("spuDescription")), pattern)
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean up(Long spuId) {
        SpuInfoEntity spu = spuInfoRepository.findById(spuId).orElse(null);
        if (spu == null) {
            return false;
        }
        List<SkuEsModel> upProducts = buildSkuEsModels(spuId, spu);
        if (upProducts.isEmpty()) {
            return false;
        }
        searchApi.productUp(upProducts);
        int rows = spuInfoRepository.updatePublishStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        return rows > 0;
    }

    @Override
    public boolean refreshSearchIndexIfOnSale(Long spuId) {
        SpuInfoEntity spu = spuInfoRepository.findById(spuId).orElse(null);
        if (spu == null || spu.getPublishStatus() == null
                || spu.getPublishStatus() != ProductConstant.StatusEnum.SPU_UP.getCode()) {
            return false;
        }
        List<SkuEsModel> models = buildSkuEsModels(spuId, spu);
        if (models.isEmpty()) {
            return false;
        }
        searchApi.productUp(models);
        return true;
    }

    private List<SkuEsModel> buildSkuEsModels(Long spuId, SpuInfoEntity spu) {
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        if (skus.isEmpty()) {
            return List.of();
        }
        String brandName = resolveBrandName(spu.getBrandId());
        String catalogName = resolveCatalogName(spu.getCatalogId());
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrIds.isEmpty() ? List.of() : attrService.selectSearchAttrIds(attrIds);
        Set<Long> searchAttrIdSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> baseEsAttrs = baseAttrs.stream()
                .filter(attr -> searchAttrIdSet.contains(attr.getAttrId()))
                .map(attr -> {
                    SkuEsModel.Attrs a = new SkuEsModel.Attrs();
                    a.setAttrId(attr.getAttrId());
                    a.setAttrValue(attr.getAttrValue());
                    return a;
                }).collect(Collectors.toList());

        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, List<SkuSaleAttrValueEntity>> saleAttrsBySkuId = skuSaleAttrValueService.listBySkuIds(skuIds).stream()
                .collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getSkuId));
        List<Long> saleAttrIds = saleAttrsBySkuId.values().stream()
                .flatMap(List::stream)
                .map(SkuSaleAttrValueEntity::getAttrId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Set<Long> searchableSaleAttrIds = saleAttrIds.isEmpty()
                ? Set.of()
                : new HashSet<>(attrService.selectSearchAttrIds(saleAttrIds));
        Map<Long, Boolean> hasStockMap = new HashMap<>();
        try {
            R wareR = wareApi.hasStock(skuIds);
            if (wareR != null && wareR.get("data") != null) {
                List<SkuHasStockVo> stockList = objectMapper.convertValue(
                        wareR.get("data"),
                        new TypeReference<List<SkuHasStockVo>>() {}
                );
                if (stockList != null) {
                    hasStockMap = stockList.stream()
                            .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock, (a, b) -> b));
                }
            }
        } catch (Exception ignored) {
        }

        List<SkuEsModel> upProducts = new ArrayList<>();
        for (SkuInfoEntity sku : skus) {
            SkuEsModel esModel = new SkuEsModel();
            esModel.setSkuId(sku.getSkuId());
            esModel.setSpuId(spuId);
            esModel.setSpuName(spu.getSpuName());
            esModel.setSkuTitle(sku.getSkuTitle());
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            esModel.setSaleCount(sku.getSaleCount() != null ? sku.getSaleCount() : 0L);
            esModel.setHasStock(hasStockMap.getOrDefault(sku.getSkuId(), false));
            esModel.setHotScore(0L);
            esModel.setBrandId(spu.getBrandId());
            esModel.setCatalogId(spu.getCatalogId());
            esModel.setBrandName(brandName);
            esModel.setCatalogName(catalogName);
            Map<Long, SkuEsModel.Attrs> mergedAttrs = new LinkedHashMap<>();
            for (SkuEsModel.Attrs a : baseEsAttrs) {
                if (a.getAttrId() != null) {
                    mergedAttrs.put(a.getAttrId(), a);
                }
            }
            List<SkuSaleAttrValueEntity> skuSaleAttrs = saleAttrsBySkuId.getOrDefault(sku.getSkuId(), List.of());
            for (SkuSaleAttrValueEntity sale : skuSaleAttrs) {
                if (sale.getAttrId() == null || !searchableSaleAttrIds.contains(sale.getAttrId())) {
                    continue;
                }
                if (!StringUtils.hasText(sale.getAttrValue())) {
                    continue;
                }
                SkuEsModel.Attrs a = new SkuEsModel.Attrs();
                a.setAttrId(sale.getAttrId());
                a.setAttrValue(sale.getAttrValue().trim());
                mergedAttrs.put(sale.getAttrId(), a);
            }
            esModel.setAttrs(new ArrayList<>(mergedAttrs.values()));
            upProducts.add(esModel);
        }
        return upProducts;
    }

    private String resolveBrandName(Long brandId) {
        if (brandId == null) {
            return "";
        }
        BrandEntity brand = brandService.getById(brandId);
        return brand != null && brand.getName() != null ? brand.getName().trim() : "";
    }

    private String resolveCatalogName(Long catalogId) {
        if (catalogId == null) {
            return "";
        }
        return categoryRepository.findById(catalogId)
                .map(CategoryEntity::getName)
                .map(String::trim)
                .orElse("");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean down(Long spuId) {
        SpuInfoEntity spu = spuInfoRepository.findById(spuId).orElse(null);
        if (spu == null) {
            return false;
        }
        searchApi.productDown(spuId);
        int rows = spuInfoRepository.updatePublishStatus(spuId, ProductConstant.StatusEnum.SPU_DOWN.getCode());
        return rows > 0;
    }
}
