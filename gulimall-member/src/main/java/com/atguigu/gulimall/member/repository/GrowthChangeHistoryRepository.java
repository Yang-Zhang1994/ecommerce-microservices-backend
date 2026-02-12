package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.GrowthChangeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowthChangeHistoryRepository extends JpaRepository<GrowthChangeHistoryEntity, Long> {
}
