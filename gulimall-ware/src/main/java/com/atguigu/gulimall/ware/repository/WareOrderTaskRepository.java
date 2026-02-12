package com.atguigu.gulimall.ware.repository;

import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WareOrderTaskRepository extends JpaRepository<WareOrderTaskEntity, Long> {
}
