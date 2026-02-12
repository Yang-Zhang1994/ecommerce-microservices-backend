package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 成长值变化历史记录
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_growth_change_history")
public class GrowthChangeHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "create_time")
    private Date createTime;

    /** 改变的值（正负计数） */
    @Column(name = "change_count")
    private Integer changeCount;

    /** 备注 */
    @Column(name = "note")
    private String note;

    /** 积分来源[0-购物，1-管理员修改] */
    @Column(name = "source_type")
    private Integer sourceType;
}
