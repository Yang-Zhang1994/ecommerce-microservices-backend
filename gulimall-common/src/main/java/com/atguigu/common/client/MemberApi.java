package com.atguigu.common.client;

import com.atguigu.common.to.member.MemberGoogleOAuthTo;
import com.atguigu.common.to.member.MemberLoginTo;
import com.atguigu.common.to.member.MemberRegisterTo;
import com.atguigu.common.utils.R;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * HTTP client for gulimall-member（注册、登录）.
 */
@HttpExchange
public interface MemberApi {

    @PostExchange("/member/member/register")
    R register(@RequestBody MemberRegisterTo body);

    @PostExchange("/member/member/login")
    R login(@RequestBody MemberLoginTo body);

    @PostExchange("/member/member/oauth/google")
    R oauthGoogle(@RequestBody MemberGoogleOAuthTo body);
}
