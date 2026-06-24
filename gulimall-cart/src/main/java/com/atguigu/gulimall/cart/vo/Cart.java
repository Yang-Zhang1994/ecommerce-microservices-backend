package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 整个购物车
 * 需要计算的属性，必须重写他的 get 方法，保证每次获取属性都会进行计算
 */
@Data
public class Cart {

    private List<CartItem> items = new ArrayList<>();

    /** 商品件数（已勾选行的数量之和） */
    private Integer countNum;

    /** 商品种类数（已勾选行数） */
    private Integer countType;

    /** 商品总价（已勾选行的小计之和，未扣减优惠） */
    private BigDecimal totalAmount;

    /** 减免价格 */
    private BigDecimal reduce = new BigDecimal("0.00");

    public Integer getCountNum() {
        int n = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (Boolean.TRUE.equals(item.getCheck())) {
                    Integer c = item.getCount();
                    if (c != null) {
                        n += c;
                    }
                }
            }
        }
        return n;
    }

    public Integer getCountType() {
        int n = 0;
        if (items != null) {
            for (CartItem item : items) {
                if (Boolean.TRUE.equals(item.getCheck()) && item.getCount() != null && item.getCount() > 0) {
                    n++;
                }
            }
        }
        return n;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal sum = BigDecimal.ZERO;
        if (items != null) {
            for (CartItem item : items) {
                if (Boolean.TRUE.equals(item.getCheck())) {
                    sum = sum.add(item.getTotalPrice());
                }
            }
        }
        return sum;
    }
}
