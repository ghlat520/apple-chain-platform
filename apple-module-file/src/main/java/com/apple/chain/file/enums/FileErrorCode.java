package com.apple.chain.file.enums;

import com.apple.chain.common.result.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * File module error codes — range 800000 ~ 809999.
 * Allocated below the existing module ranges; aligned with
 * {@code docs/contract/BACKEND-CONTRACT.md} module number policy.
 */
@Getter
@AllArgsConstructor
public enum FileErrorCode implements IErrorCode {

    FILE_EMPTY(800001, "上传文件不能为空"),
    FILE_TOO_LARGE(800002, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(800003, "不支持的文件类型"),
    FILE_NOT_FOUND(800004, "文件不存在"),
    FILE_STORAGE_ERROR(800005, "文件存储失败"),
    FILENAME_INVALID(800006, "文件名非法"),
    ;

    private final int code;
    private final String message;
}
