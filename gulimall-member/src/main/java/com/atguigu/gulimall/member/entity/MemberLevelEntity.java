package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member_level")
public class MemberLevelEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 等级名称 */
    @Column(name = "name")
    private String name;

    /** 等级需要的成长值 */
    @Column(name = "growth_point")
    private Integer growthPoint;

    /** 是否为默认等级[0->不是；1->是] */
    @Column(name = "default_status")
    private Integer defaultStatus;

    /** 免运费标准 */
    @Column(name = "free_freight_point")
    private BigDecimal freeFreightPoint;

    /** 每次评价获取的成长值 */
    @Column(name = "comment_growth_point")
    private Integer commentGrowthPoint;

    /** 是否有免邮特权 */
    @Column(name = "priviledge_free_freight")
    private Integer priviledgeFreeFreight;

    /** 是否有会员价格特权 */
    @Column(name = "priviledge_member_price")
    private Integer priviledgeMemberPrice;

    /** 是否有生日特权 */
    @Column(name = "priviledge_birthday")
    private Integer priviledgeBirthday;

    /** 备注 */
    @Column(name = "note")
    private String note;
}
