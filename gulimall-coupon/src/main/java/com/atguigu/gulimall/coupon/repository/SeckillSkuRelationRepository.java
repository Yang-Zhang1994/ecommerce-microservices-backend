package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SeckillSkuRelationRepository extends JpaRepository<SeckillSkuRelationEntity, Long> {

    @Modifying
    @Query("DELETE FROM SeckillSkuRelationEntity e WHERE e.skuId IN :skuIds")
    void deleteBySkuIdIn(@Param("skuIds") Collection<Long> skuIds);

    List<SeckillSkuRelationEntity> findByPromotionSessionId(Long promotionSessionId);

    Page<SeckillSkuRelationEntity> findByPromotionSessionId(Long promotionSessionId, Pageable pageable);
}
