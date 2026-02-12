package com.atguigu.gulimall.member.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员统计信息
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@Data
@Entity
@Table(name = "ums_member_statistics_info")
public class MemberStatisticsInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 会员id */
    @Column(name = "member_id")
    private Long memberId;

    /** 累计消费金额 */
    @Column(name = "consume_amount")
    private BigDecimal consumeAmount;

    /** 累计优惠金额 */
    @Column(name = "coupon_amount")
    private BigDecimal couponAmount;

    /** 订单数量 */
    @Column(name = "order_count")
    private Integer orderCount;

    /** 优惠券数量 */
    @Column(name = "coupon_count")
    private Integer couponCount;

    /** 评价数 */
    @Column(name = "comment_count")
    private Integer commentCount;

    /** 退货数量 */
    @Column(name = "return_order_count")
    private Integer returnOrderCount;

    /** 登录次数 */
    @Column(name = "login_count")
    private Integer loginCount;

    /** 关注数量 */
    @Column(name = "attend_count")
    private Integer attendCount;

    /** 粉丝数量 */
    @Column(name = "fans_count")
    private Integer fansCount;

    /** 收藏的商品数量 */
    @Column(name = "collect_product_count")
    private Integer collectProductCount;

    /** 收藏的专题活动数量 */
    @Column(name = "collect_subject_count")
    private Integer collectSubjectCount;

    /** 收藏的评论数量 */
    @Column(name = "collect_comment_count")
    private Integer collectCommentCount;

    /** 邀请的朋友数量 */
    @Column(name = "invite_friend_count")
    private Integer inviteFriendCount;
}
