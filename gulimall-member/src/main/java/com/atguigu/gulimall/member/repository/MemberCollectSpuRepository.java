package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberCollectSpuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberCollectSpuRepository extends JpaRepository<MemberCollectSpuEntity, Long> {
}
