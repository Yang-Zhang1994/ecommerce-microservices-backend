package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员收藏的专题活动
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member_collect_subject")
public class MemberCollectSubjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "subject_img")
    private String subjectImg;

    /** 活动url */
    @Column(name = "subject_urll")
    private String subjectUrll;
}
