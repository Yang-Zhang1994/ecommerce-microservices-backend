package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.repository.SkuInfoRepository;
import com.atguigu.gulimall.product.repository.SkuSaleAttrValueRepository;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import com.atguigu.common.utils.AttrValueSort;
import com.atguigu.gulimall.product.vo.AttrValueWithSkuIdVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.HashMap;

@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl implements SkuSaleAttrValueService {

    @Autowired
    private SkuSaleAttrValueRepository skuSaleAttrValueRepository;
    @Autowired
    private SkuInfoRepository skuInfoRepository;

    @Override
    public SkuSaleAttrValueEntity getById(Long id) {
        return skuSaleAttrValueRepository.findById(id).orElse(null);
    }

    @Override
    public void save(SkuSaleAttrValueEntity entity) {
        skuSaleAttrValueRepository.save(entity);
    }

    @Override
    public void updateById(SkuSaleAttrValueEntity entity) {
        skuSaleAttrValueRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        List<Long> longIds = ids == null ? List.of() : ids.stream()
                .map(v -> {
                    if (v instanceof Number n) {
                        return n.longValue();
                    }
                    return Long.valueOf(String.valueOf(v));
                })
                .toList();
        skuSaleAttrValueRepository.deleteAllById(longIds);
    }

    @Override
    public void saveAll(Iterable<SkuSaleAttrValueEntity> entities) {
        skuSaleAttrValueRepository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<SkuSaleAttrValueEntity>().getPageable(params, Sort.by("id").ascending());
        Page<SkuSaleAttrValueEntity> page = skuSaleAttrValueRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        if (spuId == null) {
            return List.of();
        }
        List<SkuInfoEntity> skuList = skuInfoRepository.findBySpuId(spuId);
        List<Long> skuIds = skuList.stream().map(SkuInfoEntity::getSkuId).filter(Objects::nonNull).distinct().toList();
        if (skuIds.isEmpty()) {
            return List.of();
        }
        List<SkuSaleAttrValueEntity> rows = skuSaleAttrValueRepository.findBySkuIdIn(skuIds);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        // attrId -> (value -> sku ids); sort values when building response (RAM/Capacity ascending by GB)
        Map<Long, Map<String, Set<Long>>> valueToSkuIds = new LinkedHashMap<>();
        Map<Long, String> attrNames = new LinkedHashMap<>();
        for (SkuSaleAttrValueEntity r : rows) {
            if (r.getAttrId() == null || r.getSkuId() == null) {
                continue;
            }
            attrNames.putIfAbsent(r.getAttrId(), r.getAttrName());
            String v = r.getAttrValue();
            if (v == null || v.isBlank()) {
                continue;
            }
            valueToSkuIds
                    .computeIfAbsent(r.getAttrId(), k -> new HashMap<>())
                    .computeIfAbsent(v.trim(), k -> new LinkedHashSet<>())
                    .add(r.getSkuId());
        }
        List<SkuItemSaleAttrVo> out = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Set<Long>>> e : valueToSkuIds.entrySet()) {
            SkuItemSaleAttrVo s = new SkuItemSaleAttrVo();
            s.setAttrId(e.getKey());
            String attrName = attrNames.get(e.getKey());
            s.setAttrName(attrName);
            Comparator<String> valueOrder = AttrValueSort.forAttrName(attrName);
            List<AttrValueWithSkuIdVo> vals = new ArrayList<>();
            e.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(valueOrder))
                    .forEach(ve -> {
                        AttrValueWithSkuIdVo av = new AttrValueWithSkuIdVo();
                        av.setAttrValue(ve.getKey());
                        av.setSkuIds(new ArrayList<>(ve.getValue()));
                        vals.add(av);
                    });
            s.setAttrValues(vals);
            out.add(s);
        }
        return out;
    }

    @Override
    public List<String> getSaleAttrStringsBySkuId(Long skuId) {
        if (skuId == null) {
            return List.of();
        }
        List<SkuSaleAttrValueEntity> rows = skuSaleAttrValueRepository.findBySkuId(skuId);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (SkuSaleAttrValueEntity row : rows) {
            if (row.getAttrName() == null || row.getAttrValue() == null) {
                continue;
            }
            out.add(row.getAttrName().trim() + ":" + row.getAttrValue().trim());
        }
        return out;
    }

    @Override
    public List<SkuSaleAttrValueEntity> listBySkuIds(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return skuSaleAttrValueRepository.findBySkuIdIn(skuIds);
    }
}
