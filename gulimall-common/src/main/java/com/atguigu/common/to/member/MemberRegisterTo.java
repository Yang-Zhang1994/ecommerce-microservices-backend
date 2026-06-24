package com.atguigu.common.to.member;

import lombok.Data;

import java.io.Serializable;

@Data
public class MemberRegisterTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String mobile;
    private String email;

    /** SMS code; validated only by auth server, not persisted by member service */
    private String smsCode;
}
