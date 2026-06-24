package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByUsername(String username);

    Optional<MemberEntity> findByMobile(String mobile);

    Optional<MemberEntity> findByEmail(String email);
}
