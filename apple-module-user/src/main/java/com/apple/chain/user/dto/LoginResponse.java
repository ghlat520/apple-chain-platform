package com.apple.chain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResponse {

    private final String token;
    private final Long userId;
    private final String username;
    private final String realName;
    /** Primary role code (kept for backward compatibility with existing UI code). */
    private final String roleCode;
    private final String orgName;
    private final String avatar;
    /** All role codes the user holds. */
    private final List<String> roles;
    /** Resolved permission codes (resource:action) for the user. */
    private final List<String> permissions;
}
