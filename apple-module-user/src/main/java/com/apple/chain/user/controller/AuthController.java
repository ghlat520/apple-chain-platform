package com.apple.chain.user.controller;

import com.apple.chain.common.context.UserContext;
import com.apple.chain.common.result.R;
import com.apple.chain.user.dto.LoginRequest;
import com.apple.chain.user.dto.LoginResponse;
import com.apple.chain.user.entity.User;
import com.apple.chain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication endpoints (login/logout/profile/password).
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

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

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return R.ok("密码修改成功", null);
    }
}
