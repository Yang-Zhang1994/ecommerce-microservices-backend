package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    /** 管理端树：查全部分类，不受 @Where(show_status=1) 限制，避免因 show_status 被误写为 null 导致树为空 */
    @Query(value = "SELECT * FROM pms_category ORDER BY parent_cid, sort, cat_id", nativeQuery = true)
    List<CategoryEntity> findAllForAdminTree();

    /** 全表条数，不受 @Where 限制，用于判断是否需初始化默认分类 */
    @Query(value = "SELECT COUNT(*) FROM pms_category", nativeQuery = true)
    long countAll();

    /** 一次性修复：把 show_status 为 null 的改为 1（批量保存误写后恢复显示） */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE pms_category SET show_status = 1 WHERE show_status IS NULL", nativeQuery = true)
    int fixShowStatusNull();
}
