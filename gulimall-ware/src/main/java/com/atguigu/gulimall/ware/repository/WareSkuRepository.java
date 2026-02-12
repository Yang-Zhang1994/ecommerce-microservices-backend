package com.atguigu.gulimall.ware.repository;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WareSkuRepository extends JpaRepository<WareSkuEntity, Long>, JpaSpecificationExecutor<WareSkuEntity> {

    List<WareSkuEntity> findBySkuIdAndWareId(Long skuId, Long wareId);
}
