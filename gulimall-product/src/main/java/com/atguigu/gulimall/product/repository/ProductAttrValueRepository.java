package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttrValueRepository extends JpaRepository<ProductAttrValueEntity, Long> {

    List<ProductAttrValueEntity> findBySpuId(Long spuId);

    @Modifying
    @Query("DELETE FROM ProductAttrValueEntity e WHERE e.spuId = :spuId")
    void deleteBySpuId(@Param("spuId") Long spuId);
}
