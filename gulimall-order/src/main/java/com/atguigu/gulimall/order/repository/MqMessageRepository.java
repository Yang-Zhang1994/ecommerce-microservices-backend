package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.MqMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface MqMessageRepository extends JpaRepository<MqMessageEntity, String> {

    Page<MqMessageEntity> findByMessageStatusOrderByCreateTimeAsc(Integer messageStatus, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MqMessageEntity m SET m.messageStatus = :newStatus, m.updateTime = :now WHERE m.messageId = :id AND m.messageStatus = :expected")
    int updateStatusIfMatch(
            @Param("id") String id,
            @Param("newStatus") int newStatus,
            @Param("expected") int expected,
            @Param("now") Date now
    );
}
