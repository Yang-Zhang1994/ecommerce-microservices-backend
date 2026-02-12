package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.repository.*;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.List;
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
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<AttrEntity>().getPageable(params);
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

        Specification<AttrEntity> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("attrType"), attrType);
            if (catelogId != 0) {
                p = cb.and(p, cb.equal(root.get("catelogId"), catelogId));
            }
            if (key != null && !key.isEmpty()) {
                try {
                    Long keyId = Long.parseLong(key);
                    p = cb.and(p, cb.or(
                            cb.equal(root.get("attrId"), keyId),
                            cb.like(root.get("attrName"), "%" + key + "%")
                    ));
                } catch (NumberFormatException e) {
                    p = cb.and(p, cb.like(root.get("attrName"), "%" + key + "%"));
                }
            }
            return p;
        };

        Pageable pageable = new Query<AttrEntity>().getPageable(params);
        Page<AttrEntity> page = attrRepository.findAll(spec, pageable);
        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getContent();
        List<AttrRespVo> respVoList = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            categoryRepository.findById(attrEntity.getCatelogId()).ifPresent(c -> attrRespVo.setCatelogName(c.getName()));
            if ("base".equalsIgnoreCase(type)) {
                attrAttrgroupRelationRepository.findByAttrId(attrEntity.getAttrId())
                        .flatMap(rel -> attrGroupRepository.findById(rel.getAttrGroupId()))
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
                        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                        relationEntity.setAttrGroupId(attr.getAttrGroupId());
                        relationEntity.setAttrId(attr.getAttrId());
                        attrAttrgroupRelationRepository.save(relationEntity);
                    }
            );
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
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        List<AttrAttrgroupRelationEntity> list = Arrays.stream(vos).map((vo) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationRepository.deleteAll(list);
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
                try {
                    Long keyId = Long.parseLong(key);
                    p = cb.and(p, cb.or(
                            cb.equal(root.get("attrId"), keyId),
                            cb.like(root.get("attrName"), "%" + key + "%")
                    ));
                } catch (NumberFormatException e) {
                    p = cb.and(p, cb.like(root.get("attrName"), "%" + key + "%"));
                }
            }
            return p;
        };
        Pageable pageable = new Query<AttrEntity>().getPageable(params);
        Page<AttrEntity> page = attrRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }
}
