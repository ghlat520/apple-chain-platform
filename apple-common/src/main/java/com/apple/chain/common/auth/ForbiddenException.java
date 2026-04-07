package com.apple.chain.common.auth;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;

/**
 * Raised when an authenticated user fails an RBAC permission check.
 * Mapped to HTTP 403 by {@link com.apple.chain.common.exception.GlobalExceptionHandler}
 * via the inherited {@link ResultCode#FORBIDDEN} code.
 */
public class ForbiddenException extends BizException {

    public ForbiddenException(String message) {
        super(ResultCode.FORBIDDEN, message);
    }
}
