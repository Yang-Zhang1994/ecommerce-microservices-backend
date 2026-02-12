package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.repository.AttrGroupRepository;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;

@Service("attrGroupService")
public class AttrGroupServiceImpl implements AttrGroupService {
    @Autowired
    private AttrGroupRepository attrGroupRepository;
    @Autowired
    private AttrService attrService;

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
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<AttrGroupEntity>().getPageable(params);
        Page<AttrGroupEntity> page = attrGroupRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
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
        Pageable pageable = new Query<AttrGroupEntity>().getPageable(params);
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
                vo.setAttrs(attrs);
            }
            return vo;
        }).toList();
    }
}
