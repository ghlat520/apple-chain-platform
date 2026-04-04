package com.apple.chain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private final String token;
    private final Long userId;
    private final String username;
    private final String realName;
    private final String roleCode;
    private final String orgName;
    private final String avatar;
}
