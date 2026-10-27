package com.apple.chain.user.enums;

import com.apple.chain.common.result.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User module error codes — range 100000 ~ 199999.
 *
 * <p>This is the reference implementation mandated by
 * {@code docs/contract/BACKEND-CONTRACT.md} section 4. Every other module
 * (planting, trade, warehouse, ...) should follow this template with its own
 * allocated range.
 *
 * <p>Usage in service layer:
 * <pre>{@code
 * if (user == null) {
 *     throw new BizException(UserErrorCode.USER_NOT_FOUND);
 * }
 * }</pre>
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements IErrorCode {

    USER_NOT_FOUND(100001, "用户不存在"),
    PASSWORD_INVALID(100002, "用户名或密码错误"),
    ACCOUNT_LOCKED(100003, "账号已锁定，请联系管理员"),
    OLD_PASSWORD_MISMATCH(100004, "原密码错误"),
    TOKEN_EXPIRED(100005, "登录已过期，请重新登录"),
    ROLE_NOT_FOUND(100101, "角色不存在"),
    PERMISSION_DENIED(100201, "没有执行此操作的权限"),
    ;

    private final int code;
    private final String message;
}
