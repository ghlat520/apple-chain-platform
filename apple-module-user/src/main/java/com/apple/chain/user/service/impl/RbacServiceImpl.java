package com.apple.chain.user.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.user.entity.SysPermission;
import com.apple.chain.user.entity.SysRole;
import com.apple.chain.user.mapper.SysPermissionMapper;
import com.apple.chain.user.mapper.SysRoleMapper;
import com.apple.chain.user.mapper.SysUserRoleMapper;
import com.apple.chain.user.service.RbacService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<SysRole> getRolesForUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return roleMapper.findByUserId(userId);
    }

    @Override
    public List<String> getPermissionCodesForUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return permissionMapper.findByUserId(userId).stream()
                .map(SysPermission::getPermCode)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds, String grantBy) {
        if (userId == null) {
            throw new BizException("userId 不能为空");
        }
        userRoleMapper.deleteByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            if (roleId != null) {
                userRoleMapper.insert(userId, roleId, grantBy);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesByCode(Long userId, List<String> roleCodes, String grantBy) {
        if (userId == null) {
            throw new BizException("userId 不能为空");
        }
        userRoleMapper.deleteByUserId(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleCode, roleCodes));
        if (roles.size() != roleCodes.stream().distinct().count()) {
            throw new BizException("存在未知的角色编码");
        }
        for (SysRole role : roles) {
            userRoleMapper.insert(userId, role.getId(), grantBy);
        }
    }

    @Override
    public List<SysRole> listAllRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder));
    }

    @Override
    public List<SysPermission> listAllPermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getPermCode));
    }

    @Override
    public Map<String, List<String>> getPermissionMatrix() {
        List<SysRole> roles = listAllRoles();
        Map<String, List<String>> matrix = new LinkedHashMap<>();
        for (SysRole role : roles) {
            List<String> codes = permissionMapper.findByRoleId(role.getId()).stream()
                    .map(SysPermission::getPermCode)
                    .sorted()
                    .collect(Collectors.toList());
            matrix.put(role.getRoleCode(), codes);
        }
        return matrix;
    }
}
