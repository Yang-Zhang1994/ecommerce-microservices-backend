package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuImagesRepository extends JpaRepository<SkuImagesEntity, Long> {

    List<SkuImagesEntity> findBySkuId(Long skuId);
}
