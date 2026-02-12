package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.CommentReplayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentReplayRepository extends JpaRepository<CommentReplayEntity, Long> {
}
