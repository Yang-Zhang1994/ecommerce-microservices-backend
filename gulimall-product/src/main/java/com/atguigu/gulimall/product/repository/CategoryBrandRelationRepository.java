package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryBrandRelationRepository extends JpaRepository<CategoryBrandRelationEntity, Long>, JpaSpecificationExecutor<CategoryBrandRelationEntity> {

    @Modifying
    @Query("UPDATE CategoryBrandRelationEntity e SET e.catelogName = :name WHERE e.catelogId = :catId")
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);

    List<CategoryBrandRelationEntity> findByBrandId(Long brandId);

    List<CategoryBrandRelationEntity> findByCatelogId(Long catelogId);
}
