package com.atguigu.gulimall.member.repository;

import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberLevelRepository extends JpaRepository<MemberLevelEntity, Long>, JpaSpecificationExecutor<MemberLevelEntity> {

    /** default_status: 0 = no, 1 = default level for new members */
    Optional<MemberLevelEntity> findFirstByDefaultStatus(Integer defaultStatus);
}
