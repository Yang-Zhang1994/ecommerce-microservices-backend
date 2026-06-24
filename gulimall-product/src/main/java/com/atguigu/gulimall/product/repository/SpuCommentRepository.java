package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SpuCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuCommentRepository extends JpaRepository<SpuCommentEntity, Long> {

    @Modifying
    @Query("DELETE FROM SpuCommentEntity e WHERE e.spuId = :spuId")
    void deleteBySpuId(@Param("spuId") Long spuId);

    @Modifying
    @Query("DELETE FROM SpuCommentEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") java.util.Collection<Long> skuIds);
}
