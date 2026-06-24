package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.vo.OrderDetailVo;
import com.atguigu.gulimall.order.vo.OrderListItemVo;
import org.springframework.data.domain.Page;

public interface OrderMemberQueryService {

    Page<OrderListItemVo> listMemberOrders(Long memberId, String statusTab, int pageNum, int pageSize);

    OrderDetailVo getMemberOrderDetail(Long memberId, String orderSn);
}
