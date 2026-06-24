package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项内容
 */
@Data
public class CartItem {

    private Long skuId;

    /** 是否勾选（参与结算） */
    private Boolean check = true;

    private String title;

    private String image;

    /** 销售属性展示，如：颜色:黑色;尺码:XL */
    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    /** 当前项小计：单价 × 数量 */
    public BigDecimal getTotalPrice() {
        if (price != null && count != null && count > 0) {
            return price.multiply(new BigDecimal(count.toString()));
        }
        return BigDecimal.ZERO;
    }
}
