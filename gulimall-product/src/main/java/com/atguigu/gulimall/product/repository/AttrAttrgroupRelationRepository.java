package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttrAttrgroupRelationRepository extends JpaRepository<AttrAttrgroupRelationEntity, Long> {

    Optional<AttrAttrgroupRelationEntity> findByAttrId(Long attrId);

    List<AttrAttrgroupRelationEntity> findByAttrGroupId(Long attrGroupId);

    List<AttrAttrgroupRelationEntity> findByAttrGroupIdIn(List<Long> attrGroupIds);

    java.util.Optional<AttrAttrgroupRelationEntity> findByAttrIdAndAttrGroupId(Long attrId, Long attrGroupId);
}
