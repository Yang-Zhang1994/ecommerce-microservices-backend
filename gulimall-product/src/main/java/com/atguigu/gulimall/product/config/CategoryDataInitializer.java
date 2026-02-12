package com.atguigu.gulimall.product.config;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 当 pms_category 表为空时插入默认三级分类，避免分类维护页一直显示「No Data」。
 */
@Component
@Order(1)
public class CategoryDataInitializer implements ApplicationRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CategoryDataInitializer.class);
        long total = categoryRepository.countAll();
        long visible = categoryRepository.count();
        log.info("CategoryDataInitializer: total={}, visible={}", total, visible);
        // 有部分分类 show_status=null（如根分类），会导致树无一级节点、接口返回空；一次性修复
        if (total > 0 && total > visible) {
            int updated = categoryRepository.fixShowStatusNull();
            log.info("CategoryDataInitializer: fixed show_status=1 for {} categories.", updated);
            return;
        }
        if (total > 0 && visible > 0) {
            return;
        }
        try {
            CategoryEntity l1a = save(0L, "家电、数码、手机", 1, 0);
            CategoryEntity l1b = save(0L, "女装、男装、内衣", 1, 1);
            CategoryEntity l1c = save(0L, "家居、家具、家装", 1, 2);
            CategoryEntity l2a = save(l1a.getCatId(), "手机通讯", 2, 0);
            save(l1a.getCatId(), "运营商、数码", 2, 1);
            save(l1b.getCatId(), "女装", 2, 0);
            save(l1b.getCatId(), "男装", 2, 1);
            save(l1c.getCatId(), "家居", 2, 0);
            save(l2a.getCatId(), "智能手机", 3, 0);
            save(l2a.getCatId(), "手机配件", 3, 1);
            log.info("CategoryDataInitializer: inserted default categories (pms_category was empty).");
        } catch (Exception e) {
            log.error("CategoryDataInitializer: failed to insert default categories", e);
        }
    }

    private CategoryEntity save(Long parentCid, String name, int catLevel, int sort) {
        CategoryEntity e = new CategoryEntity();
        e.setParentCid(parentCid);
        e.setName(name);
        e.setCatLevel(catLevel);
        e.setSort(sort);
        e.setShowStatus(1);
        return categoryRepository.save(e);
    }
}
