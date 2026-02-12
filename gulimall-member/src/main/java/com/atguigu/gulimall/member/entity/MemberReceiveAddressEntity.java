package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员收货地址
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member_receive_address")
public class MemberReceiveAddressEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    /** 收货人姓名 */
    @Column(name = "name")
    private String name;

    /** 电话 */
    @Column(name = "phone")
    private String phone;

    /** 邮政编码 */
    @Column(name = "post_code")
    private String postCode;

    /** 省份/直辖市 */
    @Column(name = "province")
    private String province;

    /** 城市 */
    @Column(name = "city")
    private String city;

    /** 区 */
    @Column(name = "region")
    private String region;

    /** 详细地址(街道) */
    @Column(name = "detail_address")
    private String detailAddress;

    /** 省市区代码 */
    @Column(name = "areacode")
    private String areacode;

    /** 是否默认 */
    @Column(name = "default_status")
    private Integer defaultStatus;
}
