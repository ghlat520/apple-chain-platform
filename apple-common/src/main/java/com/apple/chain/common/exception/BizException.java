package com.apple.chain.common.exception;

import com.apple.chain.common.result.IErrorCode;
import com.apple.chain.common.result.ResultCode;
import lombok.Getter;

/**
 * Business exception — thrown by the service layer with a user-facing message
 * and a stable numeric error code.
 *
 * <p>Preferred construction is {@code new BizException(UserErrorCode.USER_NOT_FOUND)}
 * or any other {@link IErrorCode} implementation. The legacy
 * {@code (String)} / {@code (int, String)} / {@code (ResultCode)} constructors
 * remain for backward compatibility with existing call sites.
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Preferred form: throw with a typed error code (module-specific or generic).
     * Accepts {@link ResultCode} as well as module-specific enums such as
     * {@code UserErrorCode} without requiring an overload per enum type.
     */
    public BizException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(IErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
