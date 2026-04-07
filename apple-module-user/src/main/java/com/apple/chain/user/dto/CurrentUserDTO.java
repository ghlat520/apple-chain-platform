package com.apple.chain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Response shape for {@code GET /api/user/auth/me}.
 * Returns the user profile alongside the resolved RBAC payload so the
 * front-end can populate its Pinia auth store in a single round trip.
 */
@Getter
@Builder
public class CurrentUserDTO {

    private final Long userId;
    private final String username;
    private final String realName;
    private final String phone;
    private final String email;
    private final String orgName;
    private final String avatar;
    /** Primary (legacy) role code. */
    private final String roleCode;
    private final List<String> roles;
    private final List<String> permissions;
}
