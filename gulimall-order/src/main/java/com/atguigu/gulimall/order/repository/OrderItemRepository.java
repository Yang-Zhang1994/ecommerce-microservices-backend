package com.atguigu.gulimall.order.repository;

import com.atguigu.gulimall.order.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    /**
     * Distinct SKU ids that appear on orders whose status is in {@code statuses}.
     */
    @Query("""
            SELECT DISTINCT oi.skuId FROM OrderItemEntity oi, OrderEntity o
            WHERE oi.orderId = o.id
              AND oi.skuId IN :skuIds
              AND o.status IN :statuses
            """)
    List<Long> findSkuIdsInActiveOrders(
            @Param("skuIds") Collection<Long> skuIds,
            @Param("statuses") Collection<Integer> statuses);

    List<OrderItemEntity> findByOrderSnInOrderByIdAsc(Collection<String> orderSns);

    List<OrderItemEntity> findByOrderSnOrderByIdAsc(String orderSn);
}
