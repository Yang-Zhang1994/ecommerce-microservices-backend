package com.atguigu.gulimall.ware.repository;

import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetailEntity, Long>, JpaSpecificationExecutor<PurchaseDetailEntity> {

    List<PurchaseDetailEntity> findByPurchaseId(Long purchaseId);
}
