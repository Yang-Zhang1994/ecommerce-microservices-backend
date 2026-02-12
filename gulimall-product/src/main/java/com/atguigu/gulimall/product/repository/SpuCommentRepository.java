package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SpuCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuCommentRepository extends JpaRepository<SpuCommentEntity, Long> {
}
