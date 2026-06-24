package com.atguigu.gulimall.product.repository;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpuInfoRepository extends JpaRepository<SpuInfoEntity, Long>, JpaSpecificationExecutor<SpuInfoEntity> {

    /** Update publish status by spu id (e.g. after product listing to ES). Native SQL for reliable DB update. */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE pms_spu_info SET publish_status = :status, update_time = CURRENT_TIMESTAMP WHERE id = :spuId", nativeQuery = true)
    int updatePublishStatus(@Param("spuId") Long spuId, @Param("status") Integer status);
}
