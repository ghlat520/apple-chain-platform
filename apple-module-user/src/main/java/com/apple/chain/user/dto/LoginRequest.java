package com.apple.chain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Reference DTO for the backend contract. See
 * {@code docs/contract/BACKEND-CONTRACT.md} section 3 — every field must carry
 * {@link Schema} metadata so the generated OpenAPI document is self-descriptive.
 */
@Getter
@Setter
@Schema(description = "登录请求体")
public class LoginRequest {

    @Schema(
            description = "用户名（手机号或邮箱）",
            example = "13800138000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(
            description = "登录密码",
            example = "Abc12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "密码不能为空")
    private String password;
}
