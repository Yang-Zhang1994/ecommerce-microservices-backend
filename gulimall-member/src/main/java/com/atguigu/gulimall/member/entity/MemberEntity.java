package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member")
public class MemberEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 会员等级id */
    @Column(name = "level_id")
    private Long levelId;

    /** 用户名 */
    @Column(name = "username")
    private String username;

    /** 密码 */
    @Column(name = "password")
    private String password;

    /** 昵称 */
    @Column(name = "nickname")
    private String nickname;

    /** 手机号码 */
    @Column(name = "mobile")
    private String mobile;

    /** 邮箱 */
    @Column(name = "email")
    private String email;

    /** 头像 */
    @Column(name = "header")
    private String header;

    /** 性别 */
    @Column(name = "gender")
    private Integer gender;

    /** 生日 */
    @Column(name = "birth")
    private Date birth;

    /** 所在城市 */
    @Column(name = "city")
    private String city;

    /** 职业 */
    @Column(name = "job")
    private String job;

    /** 个性签名 */
    @Column(name = "sign")
    private String sign;

    /** 用户来源 */
    @Column(name = "source_type")
    private Integer sourceType;

    /** 积分 */
    @Column(name = "integration")
    private Integer integration;

    /** 成长值 */
    @Column(name = "growth")
    private Integer growth;

    /** 启用状态 */
    @Column(name = "status")
    private Integer status;

    /** 注册时间 */
    @Column(name = "create_time")
    private Date createTime;
}
