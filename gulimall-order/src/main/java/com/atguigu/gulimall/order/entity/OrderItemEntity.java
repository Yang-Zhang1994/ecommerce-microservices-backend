package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_order_item")
public class OrderItemEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_sn")
    private String orderSn;

    @Column(name = "spu_id")
    private Long spuId;

    @Column(name = "spu_name")
    private String spuName;

    @Column(name = "spu_pic")
    private String spuPic;

    /** 品牌 */
    @Column(name = "spu_brand")
    private String spuBrand;

    /** 商品分类id */
    @Column(name = "category_id")
    private Long categoryId;

    /** 商品sku编号 */
    @Column(name = "sku_id")
    private Long skuId;

    /** 商品sku名字 */
    @Column(name = "sku_name")
    private String skuName;

    /** 商品sku图片 */
    @Column(name = "sku_pic")
    private String skuPic;

    /** 商品sku价格 */
    @Column(name = "sku_price")
    private BigDecimal skuPrice;

    /** 商品购买的数量 */
    @Column(name = "sku_quantity")
    private Integer skuQuantity;

    /** 商品销售属性组合（JSON） */
    @Column(name = "sku_attrs_vals")
    private String skuAttrsVals;

    /** 商品促销分解金额 */
    @Column(name = "promotion_amount")
    private BigDecimal promotionAmount;

    /** 优惠券优惠分解金额 */
    @Column(name = "coupon_amount")
    private BigDecimal couponAmount;

    /** 积分优惠分解金额 */
    @Column(name = "integration_amount")
    private BigDecimal integrationAmount;

    /** 该商品经过优惠后的分解金额 */
    @Column(name = "real_amount")
    private BigDecimal realAmount;

    /** 赠送积分 */
    @Column(name = "gift_integration")
    private Integer giftIntegration;

    /** 赠送成长值 */
    @Column(name = "gift_growth")
    private Integer giftGrowth;
}
