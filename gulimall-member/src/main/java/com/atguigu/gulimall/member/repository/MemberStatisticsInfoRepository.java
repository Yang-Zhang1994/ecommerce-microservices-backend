package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberStatisticsInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberStatisticsInfoRepository extends JpaRepository<MemberStatisticsInfoEntity, Long> {
}
