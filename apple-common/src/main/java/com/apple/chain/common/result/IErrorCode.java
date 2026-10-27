package com.apple.chain.common.result;

/**
 * Common contract for all error codes across modules.
 *
 * <p>Every module MUST define its business errors as an enum implementing this
 * interface (e.g. {@code UserErrorCode}, {@code TradeErrorCode}). The existing
 * {@link ResultCode} also implements this interface so that legacy callers can
 * keep using {@code ResultCode.FAIL} without change.
 *
 * <p>Number ranges are owned per module — see
 * {@code docs/contract/BACKEND-CONTRACT.md} section 4 for the allocation table.
 */
public interface IErrorCode {

    /**
     * Numeric error code. Unique across the platform.
     */
    int getCode();

    /**
     * Default user-facing message (Chinese).
     * May be overridden at throw site when more context is available.
     */
    String getMessage();
}
