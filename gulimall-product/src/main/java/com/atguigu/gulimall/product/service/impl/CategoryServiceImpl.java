package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.repository.CategoryRepository;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public CategoryEntity getById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public void save(CategoryEntity entity) {
        categoryRepository.save(entity);
    }

    @Override
    public void updateById(CategoryEntity entity) {
        categoryRepository.save(entity);
    }

    @Override
    public void removeByIds(java.util.Collection<?> ids) {
        categoryRepository.deleteAllById((Iterable<Long>) ids);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        Pageable pageable = new Query<CategoryEntity>().getPageable(params);
        Page<CategoryEntity> page = categoryRepository.findAll(pageable);
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAll();
        List<CategoryEntity> level1 = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == null || categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu, categoryEntities));
                    return menu;
                })
                .sorted((menu1, menu2) -> {
                    int cmp = (menu1.getSort() == null ? 0 : menu1.getSort())
                            - (menu2.getSort() == null ? 0 : menu2.getSort());
                    return cmp != 0 ? cmp : Long.compare(menu1.getCatId() == null ? 0 : menu1.getCatId(),
                            menu2.getCatId() == null ? 0 : menu2.getCatId());
                })
                .collect(Collectors.toList());
        return level1;
    }

    @Override
    public void removeMenusByIds(List<Long> list) {
        // 逻辑删除：只把 show_status 改为 0（不显示），不物理删记录
        for (Long id : list) {
            categoryRepository.findById(id).ifPresent(e -> {
                e.setShowStatus(0);
                categoryRepository.save(e);
            });
        }
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);
        return paths.toArray(new Long[0]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        // 只更新前端允许修改的字段，避免未传的 parentCid/catLevel/sort 等被覆盖导致分类从树中消失
        CategoryEntity existing = categoryRepository.findById(category.getCatId()).orElse(null);
        if (existing != null) {
            existing.setName(category.getName());
            existing.setIcon(category.getIcon());
            existing.setProductUnit(category.getProductUnit());
            categoryRepository.save(existing);
            categoryBrandRelationService.updateCategory(existing.getCatId(), existing.getName());
        }
    }

    @Override
    public void updateBatchById(List<CategoryEntity> list) {
        // 只更新 sort、parentCid、catLevel，避免用请求体覆盖 name/show_status 等导致分类“消失”
        for (CategoryEntity partial : list) {
            if (partial.getCatId() == null) continue;
            CategoryEntity existing = categoryRepository.findById(partial.getCatId()).orElse(null);
            if (existing != null) {
                if (partial.getSort() != null) existing.setSort(partial.getSort());
                if (partial.getParentCid() != null) existing.setParentCid(partial.getParentCid());
                if (partial.getCatLevel() != null) existing.setCatLevel(partial.getCatLevel());
                categoryRepository.save(existing);
            }
        }
    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        CategoryEntity byId = getById(catelogId);
        if (byId != null && byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        if (byId != null) {
            paths.add(catelogId);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(e -> root.getCatId() != null && root.getCatId().equals(e.getParentCid()))
                .map(e -> {
                    e.setChildren(getChildren(e, all));
                    return e;
                })
                .sorted((e1, e2) -> {
                    int cmp = (e1.getSort() == null ? 0 : e1.getSort())
                            - (e2.getSort() == null ? 0 : e2.getSort());
                    return cmp != 0 ? cmp : Long.compare(e1.getCatId() == null ? 0 : e1.getCatId(),
                            e2.getCatId() == null ? 0 : e2.getCatId());
                })
                .collect(Collectors.toList());
    }
}
