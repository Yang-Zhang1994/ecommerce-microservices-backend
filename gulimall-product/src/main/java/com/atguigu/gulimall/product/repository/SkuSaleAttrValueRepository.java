package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuSaleAttrValueRepository extends JpaRepository<SkuSaleAttrValueEntity, Long> {

    List<SkuSaleAttrValueEntity> findBySkuId(Long skuId);
}
