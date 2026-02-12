package com.atguigu.gulimall.ware.repository;

import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WareInfoRepository extends JpaRepository<WareInfoEntity, Long>, JpaSpecificationExecutor<WareInfoEntity> {
}
