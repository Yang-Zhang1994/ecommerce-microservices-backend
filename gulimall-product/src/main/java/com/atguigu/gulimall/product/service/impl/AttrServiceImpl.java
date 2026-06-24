package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.repository.*;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrFilterMetaVo;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service("attrService")
public class AttrServiceImpl implements AttrService {
    @Autowired
    private AttrRepository attrRepository;
    @Autowired
    private AttrAttrgroupRelationRepository relationRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AttrAttrgroupRelationRepository attrAttrgroupRelationRepository;
    @Autowired
    private AttrGroupRepository attrGroupRepository;
    @Autowired
    private CategoryService categoryService;

    @Override
    public AttrEntity getById(Long id) {
        return attrRepository.findById(id).orElse(null);
    }

    @Override
    public void save(AttrEntity entity) {
        attrRepository.save(entity);
    }

    @Override
    public void updateById(AttrEntity entity) {
        attrRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        attrRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public List<AttrEntity> listByIds(java.util.Collection<Long> ids) {
        return attrRepository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<AttrEntity>().getPageable(params, Sort.by("attrId").ascending());
        Page<AttrEntity> page = attrRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        attrRepository.save(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            Long attrId = attrEntity.getAttrId();
            Long attrGroupId = attr.getAttrGroupId();
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attrGroupId);
            relationEntity.setAttrId(attrId);
            relationRepository.save(relationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        int attrType = "base".equalsIgnoreCase(type) ?
                ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode();
        String key = (String) params.get("key");

        String enableParam = (String) params.get("enable");
        Specification<AttrEntity> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("attrType"), attrType);
            if (catelogId != 0) {
                p = cb.and(p, cb.equal(root.get("catelogId"), catelogId));
            }
            if (enableParam != null && !enableParam.isBlank()) {
                try {
                    p = cb.and(p, cb.equal(root.get("enable"), Long.parseLong(enableParam.trim())));
                } catch (NumberFormatException ignored) {
                }
            }
            if (key != null && !key.isEmpty()) {
                p = cb.and(p, attrKeyPredicate(cb, root, key));
            }
            return p;
        };

        Pageable pageable = new Query<AttrEntity>().getPageable(params, Sort.by("attrId").ascending());
        Page<AttrEntity> page = attrRepository.findAll(spec, pageable);
        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getContent();
        List<AttrRespVo> respVoList = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            if (attrEntity.getCatelogId() != null) {
                categoryRepository.findById(attrEntity.getCatelogId()).ifPresent(c -> attrRespVo.setCatelogName(c.getName()));
            }
            if ("base".equalsIgnoreCase(type)) {
                attrAttrgroupRelationRepository.findByAttrId(attrEntity.getAttrId())
                        .flatMap(rel -> rel.getAttrGroupId() != null
                                ? attrGroupRepository.findById(rel.getAttrGroupId())
                                : java.util.Optional.empty())
                        .ifPresent(g -> attrRespVo.setGroupName(g.getAttrGroupName()));
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVoList);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = getById(attrId);
        if (attrEntity == null) return null;
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        Long[] path = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(path);
        categoryRepository.findById(attrEntity.getCatelogId()).ifPresent(c -> attrRespVo.setCatelogName(c.getName()));
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            relationRepository.findByAttrId(attrId)
                    .flatMap(rel -> attrGroupRepository.findById(rel.getAttrGroupId()))
                    .ifPresent(g -> {
                        attrRespVo.setGroupName(g.getAttrGroupName());
                        attrRespVo.setAttrGroupId(g.getAttrGroupId());
                    });
        }
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        attrRepository.save(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            relationRepository.findByAttrId(attr.getAttrId()).ifPresentOrElse(
                    toUpdate -> {
                        toUpdate.setAttrGroupId(attr.getAttrGroupId());
                        attrAttrgroupRelationRepository.save(toUpdate);
                    },
                    () -> {
                        if (attr.getAttrGroupId() != null) {
                            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                            relationEntity.setAttrGroupId(attr.getAttrGroupId());
                            relationEntity.setAttrId(attr.getAttrId());
                            attrAttrgroupRelationRepository.save(relationEntity);
                        }
                    }
            );
        } else {
            // Sale attributes must not stay linked to a base attr group (Publish SPU step 2).
            relationRepository.findByAttrId(attr.getAttrId()).ifPresent(relationRepository::delete);
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> entities = relationRepository.findByAttrGroupId(attrGroupId);
        List<Long> ids = entities.stream().map(AttrAttrgroupRelationEntity::getAttrId).toList();
        if (ids.isEmpty()) return null;
        return attrRepository.findAllById(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        if (vos == null || vos.length == 0) {
            return;
        }
        for (AttrGroupRelationVo vo : vos) {
            if (vo == null || vo.getAttrId() == null || vo.getAttrGroupId() == null) {
                continue;
            }
            relationRepository.deleteByAttrIdAndAttrGroupId(vo.getAttrId(), vo.getAttrGroupId());
        }
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrGroupId) {
        AttrGroupEntity attrGroupEntity = attrGroupRepository.findById(attrGroupId).orElse(null);
        if (attrGroupEntity == null) return new PageUtils();
        Long catelogId = attrGroupEntity.getCatelogId();
        List<AttrGroupEntity> groupEntities = attrGroupRepository.findByCatelogId(catelogId);
        List<Long> groupIds = groupEntities.stream().map(AttrGroupEntity::getAttrGroupId).toList();
        List<AttrAttrgroupRelationEntity> relationEntities = relationRepository.findByAttrGroupIdIn(groupIds);
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).distinct().toList();

        String key = (String) params.get("key");
        Specification<AttrEntity> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("catelogId"), catelogId);
            p = cb.and(p, cb.equal(root.get("attrType"), ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()));
            if (!attrIds.isEmpty()) {
                p = cb.and(p, root.get("attrId").in(attrIds).not());
            }
            if (key != null && !key.isEmpty()) {
                p = cb.and(p, attrKeyPredicate(cb, root, key));
            }
            return p;
        };
        Pageable pageable = new Query<AttrEntity>().getPageable(params, Sort.by("attrId").ascending());
        Page<AttrEntity> page = attrRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    /** Match attr id (numeric key) or attr name (case-insensitive substring). */
    private Predicate attrKeyPredicate(CriteriaBuilder cb, Root<AttrEntity> root, String key) {
        String trimmed = key.trim();
        String pattern = "%" + trimmed.toLowerCase(Locale.ROOT) + "%";
        try {
            Long keyId = Long.parseLong(trimmed);
            return cb.or(
                    cb.equal(root.get("attrId"), keyId),
                    cb.like(cb.lower(root.get("attrName")), pattern)
            );
        } catch (NumberFormatException e) {
            return cb.like(cb.lower(root.get("attrName")), pattern);
        }
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        if (attrIds == null || attrIds.isEmpty()) {
            return List.of();
        }
        // searchType=1 表示可被检索
        List<AttrEntity> searchAttrs = attrRepository.findByAttrIdInAndSearchType(attrIds, 1);
        return searchAttrs.stream().map(AttrEntity::getAttrId).toList();
    }

    @Override
    public List<AttrFilterMetaVo> listSearchFilterMeta(List<Long> attrIds) {
        if (attrIds == null || attrIds.isEmpty()) {
            return List.of();
        }
        List<AttrEntity> searchAttrs = attrRepository.findByAttrIdInAndSearchType(attrIds, 1);
        List<AttrFilterMetaVo> result = new ArrayList<>();
        for (AttrEntity attr : searchAttrs) {
            if (attr == null || attr.getAttrId() == null) {
                continue;
            }
            AttrFilterMetaVo vo = new AttrFilterMetaVo();
            vo.setAttrId(attr.getAttrId());
            vo.setAttrName(attr.getAttrName() != null ? attr.getAttrName().trim() : null);
            vo.setAllowedValues(parseValueSelect(attr.getValueSelect()));
            result.add(vo);
        }
        return result;
    }

    private static List<String> parseValueSelect(String valueSelect) {
        if (valueSelect == null || valueSelect.isBlank()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String part : valueSelect.split(";")) {
            String v = part.trim();
            if (!v.isEmpty()) {
                values.add(v);
            }
        }
        return values;
    }
}
