package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.repository.AttrAttrgroupRelationRepository;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl implements AttrAttrgroupRelationService {

    @Autowired
    private AttrAttrgroupRelationRepository attrAttrgroupRelationRepository;

    @Override
    public AttrAttrgroupRelationEntity getById(Long id) {
        return attrAttrgroupRelationRepository.findById(id).orElse(null);
    }

    @Override
    public void save(AttrAttrgroupRelationEntity entity) {
        attrAttrgroupRelationRepository.save(entity);
    }

    @Override
    public void updateById(AttrAttrgroupRelationEntity entity) {
        attrAttrgroupRelationRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        attrAttrgroupRelationRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<AttrAttrgroupRelationEntity>().getPageable(params);
        Page<AttrAttrgroupRelationEntity> page = attrAttrgroupRelationRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveRelations(List<AttrGroupRelationVo> vos) {
        List<AttrAttrgroupRelationEntity> list = vos.stream().map((vo) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(vo, relationEntity);
            return relationEntity;
        }).toList();
        attrAttrgroupRelationRepository.saveAll(list);
    }
}
