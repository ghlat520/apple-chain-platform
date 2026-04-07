package com.apple.chain.user.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.user.entity.User;
import com.apple.chain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * User management CRUD endpoints (admin operations).
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表（分页）")
    @GetMapping("/list")
    @RequirePerm("user:read")
    public R<PageResult<User>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色代码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return R.ok(PageResult.of(userService.listUsers(page, size, keyword, roleCode, status)));
    }

    @Operation(summary = "创建用户")
    @PostMapping("/create")
    @RequirePerm("user:write")
    public R<User> create(@RequestBody User user) {
        return R.ok(userService.createUser(user));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @RequirePerm("user:write")
    public R<User> update(
            @PathVariable Long id,
            @RequestBody User user) {
        return R.ok(userService.updateUser(id, user));
    }

    @Operation(summary = "删除用户（软删除）")
    @DeleteMapping("/{id}")
    @RequirePerm("user:write")
    public R<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出用户CSV")
    @GetMapping("/export")
    @RequirePerm("user:read")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        userService.exportUsers(keyword, roleCode, status, response);
    }
}
