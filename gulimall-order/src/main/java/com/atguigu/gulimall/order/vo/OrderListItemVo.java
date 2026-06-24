package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Member order list row: order header + first-line item summary.
 */
@Data
public class OrderListItemVo {
    private Long id;
    private String orderSn;
    private Date createTime;
    private String memberUsername;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private Long memberId;

    /** First line item title (sku name preferred). */
    private String firstItemTitle;
    /** Sum of line quantities. */
    private Integer itemCount;
    /** Thumbnail for the first line item. */
    private String firstItemPic;

    public static OrderListItemVo from(OrderEntity order) {
        OrderListItemVo vo = new OrderListItemVo();
        if (order == null) {
            return vo;
        }
        vo.setId(order.getId());
        vo.setOrderSn(StringUtils.hasText(order.getOrderSn()) ? order.getOrderSn().trim() : order.getOrderSn());
        vo.setCreateTime(order.getCreateTime());
        vo.setMemberUsername(order.getMemberUsername());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setMemberId(order.getMemberId());
        return vo;
    }
}
