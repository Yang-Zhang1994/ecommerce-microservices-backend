package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.AttrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AttrRepository extends JpaRepository<AttrEntity, Long>, JpaSpecificationExecutor<AttrEntity> {
}
