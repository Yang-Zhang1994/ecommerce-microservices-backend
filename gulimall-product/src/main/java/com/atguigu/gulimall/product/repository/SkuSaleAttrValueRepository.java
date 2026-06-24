package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SkuSaleAttrValueRepository extends JpaRepository<SkuSaleAttrValueEntity, Long> {

    List<SkuSaleAttrValueEntity> findBySkuId(Long skuId);

    List<SkuSaleAttrValueEntity> findBySkuIdIn(Collection<Long> skuIds);

    @Modifying
    @Query("DELETE FROM SkuSaleAttrValueEntity e WHERE e.skuId = :skuId")
    void deleteBySkuId(@Param("skuId") Long skuId);

    @Modifying
    @Query("DELETE FROM SkuSaleAttrValueEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") Collection<Long> skuIds);
}
