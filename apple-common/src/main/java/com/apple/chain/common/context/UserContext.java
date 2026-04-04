package com.apple.chain.common.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ThreadLocal-based current user context.
 * Populated by AuthInterceptor after JWT verification.
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    public static String getRoleCode() {
        CurrentUser user = HOLDER.get();
        return user != null ? user.getRoleCode() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Getter
    @AllArgsConstructor
    public static class CurrentUser {
        private final Long userId;
        private final String username;
        private final String roleCode;
    }
}
