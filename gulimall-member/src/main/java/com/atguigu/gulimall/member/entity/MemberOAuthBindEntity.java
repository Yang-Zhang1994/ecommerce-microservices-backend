package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(
        name = "ums_member_oauth_bind",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_oauth_provider_subject", columnNames = {"provider", "provider_user_id"}))
public class MemberOAuthBindEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "created_at")
    private Date createdAt;
}
