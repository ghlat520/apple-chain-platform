package com.apple.chain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "登录成功后的返回体，包含 Token、基础资料与权限信息")
public class LoginResponse {

    @Schema(
            description = "JWT 访问令牌，后续请求需放入 Authorization: Bearer <token>",
            example = "eyJhbGciOiJIUzI1NiJ9...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String token;

    @Schema(description = "用户唯一 ID", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Long userId;

    @Schema(description = "登录用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String username;

    @Schema(description = "真实姓名", example = "张三")
    private final String realName;

    /** Primary role code (kept for backward compatibility with existing UI code). */
    @Schema(description = "主角色编码（向后兼容字段，优先使用 roles 列表）", example = "FARMER")
    private final String roleCode;

    @Schema(description = "所属组织名称", example = "洛川县某苹果合作社")
    private final String orgName;

    @Schema(description = "头像 URL", example = "https://cdn.example.com/avatar/1024.png")
    private final String avatar;

    /** All role codes the user holds. */
    @Schema(description = "用户持有的全部角色编码列表", example = "[\"FARMER\",\"COOP_MEMBER\"]")
    private final List<String> roles;

    /** Resolved permission codes (resource:action) for the user. */
    @Schema(description = "已解析的权限编码列表（resource:action 格式）", example = "[\"orchard:read\",\"orchard:write\"]")
    private final List<String> permissions;
}
