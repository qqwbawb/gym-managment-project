package com.xq.web.login.entity;

import lombok.Data;

@Data
public class LoginResult {

    private Long userId;
    private String userName;
    private String token;
    private String userType;
}
