package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuInfoRepository extends JpaRepository<SkuInfoEntity, Long>, JpaSpecificationExecutor<SkuInfoEntity> {

    List<SkuInfoEntity> findBySpuId(Long spuId);
}
