package com.apple.chain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for change-password endpoint with proper validation.
 */
@Getter
@Setter
@Schema(description = "修改密码请求体")
public class ChangePasswordRequest {

    @Schema(
            description = "原密码",
            example = "OldPwd123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(
            description = "新密码，长度 6-128，建议包含数字与字母",
            example = "NewPwd456",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6,
            maxLength = 128
    )
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 128, message = "新密码长度须在6-128之间")
    private String newPassword;
}
