package com.apple.chain.user.service;

import com.apple.chain.user.entity.SysPermission;
import com.apple.chain.user.entity.SysRole;

import java.util.List;
import java.util.Map;

/**
 * RBAC read/write service. Backs both the JWT issuer (login flow) and
 * the admin API ({@code /api/admin/...}).
 */
public interface RbacService {

    /** All roles assigned to the given user (sorted by sort_order). */
    List<SysRole> getRolesForUser(Long userId);

    /** Distinct permission codes available to the given user (across all their roles). */
    List<String> getPermissionCodesForUser(Long userId);

    /** Replace the user's role assignment with the given role IDs. */
    void assignRoles(Long userId, List<Long> roleIds, String grantBy);

    /** Replace the user's role assignment using role codes (more ergonomic for callers). */
    void assignRolesByCode(Long userId, List<String> roleCodes, String grantBy);

    /** Catalog of all roles. */
    List<SysRole> listAllRoles();

    /** Catalog of all permissions. */
    List<SysPermission> listAllPermissions();

    /** Role -> permission codes matrix for the admin matrix endpoint. */
    Map<String, List<String>> getPermissionMatrix();
}
