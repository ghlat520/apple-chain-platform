package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdPermission;
import com.apple.chain.bigdata.entity.BdRole;
import com.apple.chain.bigdata.entity.BdRolePermission;
import com.apple.chain.bigdata.service.BdRoleService;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role / permission matrix. Distinct from User role (legacy string code).
 * Lives under /api/bigdata/role to avoid colliding with apple-module-user.
 */
@Tag(name = "大数据-权限矩阵")
@RestController
@RequestMapping("/api/bigdata/role")
@RequiredArgsConstructor
public class RolePermissionController {

    private final BdRoleService service;

    @Operation(summary = "列出所有角色")
    @GetMapping
    public R<List<BdRole>> listRoles() {
        return R.ok(service.list());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public R<BdRole> createRole(@RequestBody BdRole body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public R<BdRole> updateRole(@PathVariable Long id, @RequestBody BdRole body) {
        body.setId(id);
        service.updateById(body);
        return R.ok(body);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteRole(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "列出所有权限")
    @GetMapping("/permissions")
    public R<List<BdPermission>> listPermissions() {
        return R.ok(service.listPermissions());
    }

    @Operation(summary = "新增权限")
    @PostMapping("/permissions")
    public R<BdPermission> createPermission(@RequestBody BdPermission body) {
        return R.ok(service.createPermission(body));
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/permissions/{id}")
    public R<Boolean> deletePermission(@PathVariable Long id) {
        return R.ok(service.deletePermission(id));
    }

    @Operation(summary = "查询角色已绑定权限")
    @GetMapping("/{roleCode}/permissions")
    public R<List<BdRolePermission>> listRolePermissions(@PathVariable String roleCode) {
        return R.ok(service.listRolePermissions(roleCode));
    }

    @Operation(summary = "覆盖式分配角色权限")
    @PostMapping("/{roleCode}/permissions")
    public R<Integer> assignPermissions(@PathVariable String roleCode,
                                        @RequestBody List<String> permCodes) {
        return R.ok(service.assignPermissions(roleCode, permCodes));
    }
}
