package com.apple.chain.common.result;

import lombok.Getter;

/**
 * Platform-wide generic result codes (0 ~ 99999 range).
 *
 * <p>Module-specific business errors live in their own enums (e.g.
 * {@code UserErrorCode}) — all of which implement {@link IErrorCode} just like
 * this enum does, so they can be passed interchangeably to
 * {@link com.apple.chain.common.exception.BizException} and {@link R#fail}.
 */
@Getter
public enum ResultCode implements IErrorCode {

    SUCCESS(200, "操作成功"),
    FAIL(400, "操作失败"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(422, "参数校验失败"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
