package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.repository.AttrAttrgroupRelationRepository;
import com.atguigu.gulimall.product.repository.AttrGroupRepository;
import com.atguigu.gulimall.product.repository.AttrRepository;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("attrGroupService")
public class AttrGroupServiceImpl implements AttrGroupService {
    @Autowired
    private AttrGroupRepository attrGroupRepository;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private AttrAttrgroupRelationRepository attrAttrgroupRelationRepository;
    @Autowired
    private AttrRepository attrRepository;

    @Override
    public AttrGroupEntity getById(Long id) {
        return attrGroupRepository.findById(id).orElse(null);
    }

    @Override
    public void save(AttrGroupEntity entity) {
        attrGroupRepository.save(entity);
    }

    @Override
    public void updateById(AttrGroupEntity entity) {
        attrGroupRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        attrGroupRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<AttrGroupEntity>().getPageable(params, Sort.by("attrGroupId").ascending());
        Page<AttrGroupEntity> page = attrGroupRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        Specification<AttrGroupEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (key != null && !key.isEmpty()) {
                try {
                    Long keyId = Long.parseLong(key);
                    p = cb.or(cb.equal(root.get("attrGroupId"), keyId), cb.like(root.get("attrGroupName"), "%" + key + "%"));
                } catch (NumberFormatException e) {
                    p = cb.like(root.get("attrGroupName"), "%" + key + "%");
                }
            }
            if (catelogId != 0) {
                p = cb.and(p, cb.equal(root.get("catelogId"), catelogId));
            }
            return p;
        };
        Pageable pageable = new Query<AttrGroupEntity>().getPageable(params, Sort.by("attrGroupId").ascending());
        Page<AttrGroupEntity> page = attrGroupRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> entities = attrGroupRepository.findByCatelogId(catelogId);
        return entities.stream().map(group -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, vo);
            List<AttrEntity> attrs = attrService.getRelationAttr(group.getAttrGroupId());
            if (attrs != null && !attrs.isEmpty()) {
                // Publish SPU step 2: only enabled base attrs (RAM/Capacity are sale attrs in step 3).
                List<AttrEntity> baseOnly = attrs.stream()
                        .filter(a -> a.getAttrType() != null
                                && a.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
                        .filter(a -> a.getEnable() == null || a.getEnable() == 1)
                        .toList();
                if (!baseOnly.isEmpty()) {
                    vo.setAttrs(baseOnly);
                }
            }
            return vo;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        if (spuId == null) {
            return List.of();
        }
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        if (baseAttrs == null || baseAttrs.isEmpty()) {
            return List.of();
        }
        Set<Long> attrIds = baseAttrs.stream()
                .map(ProductAttrValueEntity::getAttrId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, AttrEntity> attrById = attrIds.isEmpty()
                ? Map.of()
                : attrRepository.findAllById(attrIds).stream()
                        .collect(Collectors.toMap(AttrEntity::getAttrId, a -> a, (a, b) -> a));
        Map<Long, List<ProductAttrValueEntity>> byGroup = new LinkedHashMap<>();
        for (ProductAttrValueEntity pav : baseAttrs) {
            if (pav.getAttrId() == null || !isQuickDisplayAttr(pav, attrById.get(pav.getAttrId()))) {
                continue;
            }
            Long groupId = attrAttrgroupRelationRepository.findByAttrId(pav.getAttrId())
                    .map(rel -> rel.getAttrGroupId())
                    .orElse(null);
            if (groupId == null) {
                continue;
            }
            AttrGroupEntity group = attrGroupRepository.findById(groupId).orElse(null);
            if (group == null) {
                continue;
            }
            if (catalogId != null && !catalogId.equals(group.getCatelogId())) {
                continue;
            }
            byGroup.computeIfAbsent(groupId, k -> new ArrayList<>()).add(pav);
        }
        if (byGroup.isEmpty()) {
            return List.of();
        }
        Map<Long, AttrGroupEntity> groupMap = attrGroupRepository.findAllById(byGroup.keySet()).stream()
                .collect(Collectors.toMap(AttrGroupEntity::getAttrGroupId, g -> g, (a, b) -> a));
        List<Long> orderedGroupIds = byGroup.keySet().stream()
                .sorted(Comparator.comparing(gid -> {
                    AttrGroupEntity g = groupMap.get(gid);
                    return g != null && g.getSort() != null ? g.getSort() : Integer.MAX_VALUE;
                }))
                .toList();
        List<SpuItemAttrGroupVo> result = new ArrayList<>();
        for (Long gid : orderedGroupIds) {
            List<ProductAttrValueEntity> list = byGroup.get(gid);
            AttrGroupEntity ge = groupMap.get(gid);
            SpuItemAttrGroupVo gvo = new SpuItemAttrGroupVo();
            gvo.setGroupId(gid);
            gvo.setGroupName(ge != null ? ge.getAttrGroupName() : "Specifications");
            gvo.setAttrs(list.stream().map(p -> {
                SpuItemAttrVo a = new SpuItemAttrVo();
                a.setAttrId(p.getAttrId());
                a.setAttrName(p.getAttrName());
                a.setAttrValue(p.getAttrValue());
                return a;
            }).collect(Collectors.toList()));
            result.add(gvo);
        }
        return result;
    }

    /**
     * Quick Display on publish saves pms_product_attr_value.quick_show.
     * Fall back to pms_attr.show_desc when quick_show is null (legacy rows).
     */
    private static boolean isQuickDisplayAttr(ProductAttrValueEntity pav, AttrEntity attr) {
        if (pav.getQuickShow() != null) {
            return pav.getQuickShow() == 1;
        }
        return attr != null && attr.getShowDesc() != null && attr.getShowDesc() == 1;
    }
}
