package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.repository.BrandRepository;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.Map;

@Service("brandService")
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    @Lazy
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public BrandEntity getById(Long id) {
        return brandRepository.findById(id).orElse(null);
    }

    @Override
    public void save(BrandEntity entity) {
        brandRepository.save(entity);
    }

    @Override
    public void updateById(BrandEntity entity) {
        brandRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        brandRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        Specification<BrandEntity> spec = (root, query, cb) -> {
            if (key == null || key.isEmpty()) return cb.conjunction();
            try {
                Long keyId = Long.parseLong(key);
                return cb.or(cb.equal(root.get("brandId"), keyId), cb.like(root.get("name"), "%" + key + "%"));
            } catch (NumberFormatException e) {
                return cb.like(root.get("name"), "%" + key + "%");
            }
        };
        Pageable pageable = new Query<BrandEntity>().getPageable(params);
        Page<BrandEntity> page = brandRepository.findAll(spec, pageable);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        brandRepository.save(brand);
        if (brand.getName() != null && !brand.getName().isEmpty()) {
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
        }
    }
}
