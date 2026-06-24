package com.atguigu.gulimall.coupon.repository;

import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SeckillSessionRepository extends JpaRepository<SeckillSessionEntity, Long> {

    List<SeckillSessionEntity> findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Integer status, Date startUpperBound, Date endLowerBound);
}
