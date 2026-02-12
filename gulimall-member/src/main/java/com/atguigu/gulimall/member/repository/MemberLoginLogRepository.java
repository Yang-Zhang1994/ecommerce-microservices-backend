package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberLoginLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberLoginLogRepository extends JpaRepository<MemberLoginLogEntity, Long> {
}
