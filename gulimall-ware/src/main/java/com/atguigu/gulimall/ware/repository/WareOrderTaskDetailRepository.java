package com.atguigu.gulimall.ware.repository;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WareOrderTaskDetailRepository extends JpaRepository<WareOrderTaskDetailEntity, Long> {
}
