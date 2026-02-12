package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuInfoRepository extends JpaRepository<SpuInfoEntity, Long>, JpaSpecificationExecutor<SpuInfoEntity> {
}
