package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.repository.BrandRepository;
import com.atguigu.gulimall.product.repository.CategoryBrandRelationRepository;
import com.atguigu.gulimall.product.repository.CategoryRepository;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;

@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl implements CategoryBrandRelationService {

    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryBrandRelationRepository relationRepository;
    @Autowired
    @Lazy
    private BrandService brandService;

    @Override
    public CategoryBrandRelationEntity getById(Long id) {
        return relationRepository.findById(id).orElse(null);
    }

    @Override
    public void save(CategoryBrandRelationEntity entity) {
        relationRepository.save(entity);
    }

    @Override
    public void updateById(CategoryBrandRelationEntity entity) {
        relationRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        relationRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public List<CategoryBrandRelationEntity> listByBrandId(Long brandId) {
        return relationRepository.findByBrandId(brandId);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CategoryBrandRelationEntity>().getPageable(params);
        Page<CategoryBrandRelationEntity> page = relationRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandRepository.findById(brandId).orElse(null);
        CategoryEntity categoryEntity = categoryRepository.findById(catelogId).orElse(null);
        if (brandEntity != null) categoryBrandRelation.setBrandName(brandEntity.getName());
        if (categoryEntity != null) categoryBrandRelation.setCatelogName(categoryEntity.getName());
        relationRepository.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        List<CategoryBrandRelationEntity> list = relationRepository.findByBrandId(brandId);
        list.forEach(e -> {
            e.setBrandName(name);
            relationRepository.save(e);
        });
    }

    @Override
    public void updateCategory(Long catId, String name) {
        relationRepository.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandById(Long catId) {
        List<CategoryBrandRelationEntity> entities = relationRepository.findByCatelogId(catId);
        return entities.stream()
                .map(e -> brandService.getById(e.getBrandId()))
                .filter(b -> b != null)
                .toList();
    }
}
