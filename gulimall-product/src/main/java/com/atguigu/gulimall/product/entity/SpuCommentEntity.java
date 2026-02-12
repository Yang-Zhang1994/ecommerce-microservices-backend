package com.atguigu.gulimall.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品评价
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-11-20 17:10:37
 */
@Data
@Entity
@Table(name = "pms_spu_comment")
public class SpuCommentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "spu_id")
    private Long spuId;

    /** 商品名字 */
    @Column(name = "spu_name")
    private String spuName;

    /** 会员昵称 */
    @Column(name = "member_nick_name")
    private String memberNickName;

    /** 星级 */
    @Column(name = "star")
    private Integer star;

    /** 会员ip */
    @Column(name = "member_ip")
    private String memberIp;

    /** 创建时间 */
    @Column(name = "create_time")
    private Date createTime;

    /** 显示状态[0-不显示，1-显示] */
    @Column(name = "show_status")
    private Integer showStatus;

    /** 购买时属性组合 */
    @Column(name = "spu_attributes")
    private String spuAttributes;

    /** 点赞数 */
    @Column(name = "likes_count")
    private Integer likesCount;

    /** 回复数 */
    @Column(name = "reply_count")
    private Integer replyCount;

    /** 评论图片/视频[json数据；[{type:文件类型,url:资源路径}]] */
    @Column(name = "resources")
    private String resources;

    /** 内容 */
    @Column(name = "content")
    private String content;

    /** 用户头像 */
    @Column(name = "member_icon")
    private String memberIcon;

    /** 评论类型[0 - 对商品的直接评论，1 - 对评论的回复] */
    @Column(name = "comment_type")
    private Integer commentType;
}
