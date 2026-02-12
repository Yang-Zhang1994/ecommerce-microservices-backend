package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberCollectSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberCollectSubjectRepository extends JpaRepository<MemberCollectSubjectEntity, Long> {
}
