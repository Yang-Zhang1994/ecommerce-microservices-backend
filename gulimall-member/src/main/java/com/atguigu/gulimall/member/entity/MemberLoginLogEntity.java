package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员登录记录
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member_login_log")
public class MemberLoginLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** ip */
    @Column(name = "ip")
    private String ip;

    @Column(name = "city")
    private String city;

    /** 登录类型[1-web，2-app] */
    @Column(name = "login_type")
    private Integer loginType;
}
