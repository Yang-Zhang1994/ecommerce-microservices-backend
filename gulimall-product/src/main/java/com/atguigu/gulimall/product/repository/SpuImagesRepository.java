package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpuImagesRepository extends JpaRepository<SpuImagesEntity, Long> {

    List<SpuImagesEntity> findBySpuId(Long spuId);
}
