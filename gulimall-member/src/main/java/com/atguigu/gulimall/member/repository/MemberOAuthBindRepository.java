package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberOAuthBindEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberOAuthBindRepository extends JpaRepository<MemberOAuthBindEntity, Long> {

    Optional<MemberOAuthBindEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
