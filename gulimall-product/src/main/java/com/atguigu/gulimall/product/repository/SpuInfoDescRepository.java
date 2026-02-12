package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuInfoDescRepository extends JpaRepository<SpuInfoDescEntity, Long> {
}
