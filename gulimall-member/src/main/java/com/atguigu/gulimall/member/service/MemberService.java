package com.atguigu.gulimall.member.service;

import com.atguigu.common.to.member.MemberGoogleOAuthTo;
import com.atguigu.common.to.member.MemberLoginTo;
import com.atguigu.common.to.member.MemberRegisterTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Collection;
import java.util.Map;

public interface MemberService {

    PageUtils queryPage(Map<String, Object> params);
    MemberEntity getById(Long id);
    void save(MemberEntity entity);
    void updateById(MemberEntity entity);

    /** Toggle {@code ums_member.status} (0 = disabled, 1 = enabled) without touching other fields. */
    void updateStatus(Long id, Integer status);

    void removeByIds(Collection<?> ids);

    /**
     * @return member profile map for API payload (no {@code R} wrapper)
     */
    Map<String, Object> register(MemberRegisterTo to);

    Map<String, Object> login(MemberLoginTo to);

    /** Link or create member from OAuth provider claims (e.g. Google {@code sub}). */
    Map<String, Object> oauthGoogle(MemberGoogleOAuthTo to);
}
