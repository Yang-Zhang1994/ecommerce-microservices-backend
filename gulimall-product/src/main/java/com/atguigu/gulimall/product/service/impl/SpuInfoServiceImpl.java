package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.common.client.CouponApi;
import com.atguigu.gulimall.product.repository.SpuInfoRepository;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("spuInfoService")
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SpuInfoRepository spuInfoRepository;
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
        spuInfoRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        spuInfoRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SpuInfoEntity>().getPageable(params);
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
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String brandId = (String) params.get("brandId");
        String catelogId = (String) params.get("catelogId");

        Specification<SpuInfoEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (key != null && !key.isEmpty()) {
                try {
                    Long keyId = Long.parseLong(key);
                    p = cb.and(p, cb.or(
                            cb.equal(root.get("id"), keyId),
                            cb.like(root.get("spuName"), "%" + key + "%")
                    ));
                } catch (NumberFormatException e) {
                    p = cb.and(p, cb.like(root.get("spuName"), "%" + key + "%"));
                }
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
        Pageable pageable = new Query<SpuInfoEntity>().getPageable(params);
        Page<SpuInfoEntity> page = spuInfoRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }
}
