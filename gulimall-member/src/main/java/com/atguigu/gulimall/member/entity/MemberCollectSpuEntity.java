package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员收藏的商品
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member_collect_spu")
public class MemberCollectSpuEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 会员id */
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "spu_id")
    private Long spuId;

    @Column(name = "spu_name")
    private String spuName;

    @Column(name = "spu_img")
    private String spuImg;

    @Column(name = "create_time")
    private Date createTime;
}
