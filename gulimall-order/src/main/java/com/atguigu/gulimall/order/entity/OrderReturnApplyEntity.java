package com.atguigu.gulimall.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单退货申请
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:54:19
 */
@Data
@Entity
@Table(name = "oms_order_return_apply")
public class OrderReturnApplyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    /** 退货商品id */
    @Column(name = "sku_id")
    private Long skuId;

    /** 订单编号 */
    @Column(name = "order_sn")
    private String orderSn;

    /** 申请时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 会员用户名 */
    @Column(name = "member_username")
    private String memberUsername;

    /** 退款金额 */
    @Column(name = "return_amount")
    private BigDecimal returnAmount;

    /** 退货人姓名 */
    @Column(name = "return_name")
    private String returnName;

    /** 退货人电话 */
    @Column(name = "return_phone")
    private String returnPhone;

    /** 申请状态[0->待处理；1->退货中；2->已完成；3->已拒绝] */
    @Column(name = "status")
    private Integer status;

    /** 处理时间 */
    @Column(name = "handle_time")
    private Date handleTime;

    /** 商品图片 */
    @Column(name = "sku_img")
    private String skuImg;

    /** 商品名称 */
    @Column(name = "sku_name")
    private String skuName;

    /** 商品品牌 */
    @Column(name = "sku_brand")
    private String skuBrand;

    /** 商品销售属性(JSON) */
    @Column(name = "sku_attrs_vals")
    private String skuAttrsVals;

    /** 退货数量 */
    @Column(name = "sku_count")
    private Integer skuCount;

    /** 商品单价 */
    @Column(name = "sku_price")
    private BigDecimal skuPrice;

    /** 商品实际支付单价 */
    @Column(name = "sku_real_price")
    private BigDecimal skuRealPrice;

    /** 原因 */
    @Column(name = "reason")
    private String reason;

    /** 描述 (DB column name preserved) */
    @Column(name = "description述")
    private String description述;

    /** 凭证图片，以逗号隔开 */
    @Column(name = "desc_pics")
    private String descPics;

    /** 处理备注 */
    @Column(name = "handle_note")
    private String handleNote;

    /** 处理人员 */
    @Column(name = "handle_man")
    private String handleMan;

    /** 收货人 */
    @Column(name = "receive_man")
    private String receiveMan;

    /** 收货时间 */
    @Column(name = "receive_time")
    private Date receiveTime;

    /** 收货备注 */
    @Column(name = "receive_note")
    private String receiveNote;

    /** 收货电话 */
    @Column(name = "receive_phone")
    private String receivePhone;

    /** 公司收货地址 */
    @Column(name = "company_address")
    private String companyAddress;
}
