package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.repository.ProductAttrValueRepository;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("productAttrValueService")
public class ProductAttrValueServiceImpl implements ProductAttrValueService {

    @Autowired
    private ProductAttrValueRepository productAttrValueRepository;

    @Override
    public ProductAttrValueEntity getById(Long id) {
        return productAttrValueRepository.findById(id).orElse(null);
    }

    @Override
    public void save(ProductAttrValueEntity entity) {
        productAttrValueRepository.save(entity);
    }

    @Override
    public void updateById(ProductAttrValueEntity entity) {
        productAttrValueRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        productAttrValueRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<ProductAttrValueEntity>().getPageable(params);
        Page<ProductAttrValueEntity> page = productAttrValueRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> list) {
        productAttrValueRepository.saveAll(list);
    }

    @Override
    public List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId) {
        return productAttrValueRepository.findBySpuId(spuId);
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        productAttrValueRepository.deleteBySpuId(spuId);
        entities.forEach(entity -> entity.setSpuId(spuId));
        productAttrValueRepository.saveAll(entities);
    }
}
