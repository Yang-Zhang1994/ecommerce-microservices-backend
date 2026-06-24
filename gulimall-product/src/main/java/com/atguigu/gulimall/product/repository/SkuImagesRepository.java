package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SkuImagesRepository extends JpaRepository<SkuImagesEntity, Long> {

    List<SkuImagesEntity> findBySkuId(Long skuId);

    @Modifying
    @Query("DELETE FROM SkuImagesEntity e WHERE e.skuId = :skuId")
    void deleteBySkuId(@Param("skuId") Long skuId);

    @Modifying
    @Query("DELETE FROM SkuImagesEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") Collection<Long> skuIds);
}
