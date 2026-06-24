package com.atguigu.common.to.member;

import lombok.Data;

/**
 * Internal payload from auth-server after Google OAuth (maps from {@code OAuth2User} attributes).
 */
@Data
public class MemberGoogleOAuthTo {

    /** e.g. {@code google} */
    private String provider;

    /** OpenID {@code sub} */
    private String subject;

    private String email;
    private String name;
    private String picture;
}
