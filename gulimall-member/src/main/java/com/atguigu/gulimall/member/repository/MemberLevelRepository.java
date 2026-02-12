package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberLevelRepository extends JpaRepository<MemberLevelEntity, Long> {
}
