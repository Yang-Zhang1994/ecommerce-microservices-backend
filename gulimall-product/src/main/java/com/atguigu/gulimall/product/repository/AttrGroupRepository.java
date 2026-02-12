package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttrGroupRepository extends JpaRepository<AttrGroupEntity, Long>, JpaSpecificationExecutor<AttrGroupEntity> {

    List<AttrGroupEntity> findByCatelogId(Long catelogId);
}
