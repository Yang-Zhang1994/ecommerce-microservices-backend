package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.AttrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttrRepository extends JpaRepository<AttrEntity, Long>, JpaSpecificationExecutor<AttrEntity> {

    /** 在给定 attrId 中筛选出可检索的（searchType=1） */
    List<AttrEntity> findByAttrIdInAndSearchType(List<Long> attrIds, Integer searchType);
}
