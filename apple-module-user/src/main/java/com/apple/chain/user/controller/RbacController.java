package com.apple.chain.user.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.context.UserContext;
import com.apple.chain.common.result.R;
import com.apple.chain.user.dto.AssignRolesRequest;
import com.apple.chain.user.entity.SysPermission;
import com.apple.chain.user.entity.SysRole;
import com.apple.chain.user.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin RBAC endpoints. Mounted under {@code /api/admin/...} so the
 * {@link com.apple.chain.common.auth.RbacInterceptor} enforces the
 * {@link RequirePerm} annotations on every handler.
 */
@Tag(name = "RBAC 管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;

    @Operation(summary = "角色列表")
    @GetMapping("/roles")
    @RequirePerm("role:read")
    public R<List<SysRole>> listRoles() {
        return R.ok(rbacService.listAllRoles());
    }

    @Operation(summary = "权限点列表")
    @GetMapping("/permissions")
    @RequirePerm("role:read")
    public R<List<SysPermission>> listPermissions() {
        return R.ok(rbacService.listAllPermissions());
    }

    @Operation(summary = "角色-权限矩阵（roleCode -> permCode[]）")
    @GetMapping("/permissions/matrix")
    @RequirePerm("role:read")
    public R<Map<String, List<String>>> matrix() {
        return R.ok(rbacService.getPermissionMatrix());
    }

    @Operation(summary = "为用户分配角色（按角色编码）")
    @PostMapping("/users/{id}/roles")
    @RequirePerm("role:write")
    public R<Void> assignRoles(@PathVariable("id") Long userId,
                               @Valid @RequestBody AssignRolesRequest request) {
        String operator = UserContext.getUsername();
        rbacService.assignRolesByCode(userId, request.getRoleCodes(), operator);
        return R.ok("角色已更新", null);
    }

    @Operation(summary = "查询用户当前角色")
    @GetMapping("/users/{id}/roles")
    @RequirePerm("role:read")
    public R<List<SysRole>> getUserRoles(@PathVariable("id") Long userId) {
        return R.ok(rbacService.getRolesForUser(userId));
    }
}
