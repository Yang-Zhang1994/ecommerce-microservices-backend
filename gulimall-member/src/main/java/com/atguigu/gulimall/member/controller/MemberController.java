package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.common.exception.BisCodeEnum;
import com.atguigu.common.to.member.MemberGoogleOAuthTo;
import com.atguigu.common.to.member.MemberLoginTo;
import com.atguigu.common.to.member.MemberRegisterTo;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.client.CouponApi;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 会员
 *
 * @author Samuel
 * @email sc20190702@gmail.com
 * @date 2025-12-01 22:39:02
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private CouponApi couponApi;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * Enable or disable a member account (status 0/1 only).
     */
    @PostMapping("/update/status")
    public R updateStatus(@RequestBody Map<String, Object> body) {
        Object idObj = body != null ? body.get("id") : null;
        Object statusObj = body != null ? body.get("status") : null;
        if (idObj == null || statusObj == null) {
            return R.error(BisCodeEnum.VALID_EXCEPTION.getCode(), BisCodeEnum.VALID_EXCEPTION.getMessage());
        }
        Long id = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(String.valueOf(idObj));
        Integer status =
                statusObj instanceof Number ? ((Number) statusObj).intValue() : Integer.parseInt(String.valueOf(statusObj));
        memberService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 测试获取会员优惠券
     */
    /**
     * 会员注册（密码 BCrypt 存储）
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterTo vo) {
        return R.ok().put("member", memberService.register(vo));
    }

    /**
     * 会员登录
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginTo vo) {
        return R.ok().put("member", memberService.login(vo));
    }

    /**
     * Called by auth-server after Google OAuth: create/bind {@code ums_member} row.
     */
    @PostMapping("/oauth/google")
    public R oauthGoogle(@RequestBody MemberGoogleOAuthTo vo) {
        return R.ok().put("member", memberService.oauthGoogle(vo));
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        try {
            R membercoupons = couponApi.memberCoupons();
            Object coupons = membercoupons != null ? membercoupons.get("coupons") : null;
            return R.ok().put("member", memberEntity).put("coupons", coupons != null ? coupons : java.util.Collections.emptyList());
        } catch (Exception e) {
            log.warn("Coupon API call failed for /member/member/coupons", e);
            return R.ok().put("member", memberEntity).put("coupons", java.util.Collections.emptyList())
                    .put("message", "Coupon service unavailable: " + e.getMessage());
        }
    }

}
