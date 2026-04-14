package com.apple.chain.user.controller;

import com.apple.chain.common.auth.PermissionContext;
import com.apple.chain.common.context.UserContext;
import com.apple.chain.common.result.R;
import com.apple.chain.user.dto.ChangePasswordRequest;
import com.apple.chain.user.dto.CurrentUserDTO;
import com.apple.chain.user.dto.LoginRequest;
import com.apple.chain.user.dto.LoginResponse;
import com.apple.chain.user.dto.SmsLoginRequest;
import com.apple.chain.user.dto.SmsSendRequest;
import com.apple.chain.user.entity.SysRole;
import com.apple.chain.user.entity.User;
import com.apple.chain.user.service.RbacService;
import com.apple.chain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentication endpoints (login/logout/profile/password).
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RbacService rbacService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(userService.login(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        UserContext.clear();
        return R.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public R<User> profile() {
        Long userId = UserContext.getUserId();
        return R.ok(userService.getProfile(userId));
    }

    @Operation(summary = "获取当前用户 + 角色 + 权限（前端 store 初始化用）")
    @GetMapping("/me")
    public R<CurrentUserDTO> me() {
        Long userId = UserContext.getUserId();
        User user = userService.getProfile(userId);

        List<SysRole> roles = rbacService.getRolesForUser(userId);
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        // Re-use PermissionContext (already populated by RbacInterceptor) so /me reflects
        // exactly what the JWT carries — keeping the front-end in sync with token claims.
        List<String> perms = new ArrayList<>(PermissionContext.getPermissions());
        if (perms.isEmpty()) {
            perms = rbacService.getPermissionCodesForUser(userId);
        }

        return R.ok(CurrentUserDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .orgName(user.getOrgName())
                .avatar(user.getAvatar())
                .roleCode(user.getRoleCode())
                .roles(roleCodes)
                .permissions(perms)
                .build());
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, body.getOldPassword(), body.getNewPassword());
        return R.ok("密码修改成功", null);
    }

    // Legacy alias — delegates to the same service method

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public R<Void> sendSmsCode(@Valid @RequestBody SmsSendRequest request) {
        userService.sendSmsCode(request.getPhone());
        return R.ok("验证码已发送", null);
    }

    @Operation(summary = "短信验证码登录（手机号不存在则自动注册为果农）")
    @PostMapping("/sms/login")
    public R<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return R.ok(userService.smsLogin(request));
    }
}
