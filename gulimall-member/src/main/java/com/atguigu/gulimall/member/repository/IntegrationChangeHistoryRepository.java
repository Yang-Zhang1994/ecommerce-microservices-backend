package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.IntegrationChangeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationChangeHistoryRepository extends JpaRepository<IntegrationChangeHistoryEntity, Long> {
}
