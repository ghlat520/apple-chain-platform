package com.apple.chain.user.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.user.entity.SysPermission;
import com.apple.chain.user.entity.SysRole;
import com.apple.chain.user.mapper.SysPermissionMapper;
import com.apple.chain.user.mapper.SysRoleMapper;
import com.apple.chain.user.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("RbacServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class RbacServiceImplTest {

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysPermissionMapper permissionMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private RbacServiceImpl service;

    private SysRole admin;
    private SysRole farmer;

    @BeforeEach
    void setUp() {
        admin = new SysRole();
        admin.setId(1L);
        admin.setRoleCode("ADMIN");
        admin.setRoleName("管理员");
        admin.setSortOrder(1);

        farmer = new SysRole();
        farmer.setId(2L);
        farmer.setRoleCode("FARMER");
        farmer.setRoleName("种植户");
        farmer.setSortOrder(10);
    }

    @Test
    @DisplayName("getRolesForUser - userId 为空返回空列表")
    void getRolesForUser_nullUserId_returnsEmpty() {
        assertThat(service.getRolesForUser(null)).isEmpty();
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("getRolesForUser - 调用 mapper 并返回结果")
    void getRolesForUser_returnsRoles() {
        when(roleMapper.findByUserId(100L)).thenReturn(List.of(admin, farmer));

        List<SysRole> result = service.getRolesForUser(100L);

        assertThat(result).extracting(SysRole::getRoleCode).containsExactly("ADMIN", "FARMER");
    }

    @Test
    @DisplayName("getPermissionCodesForUser - 去重后返回")
    void getPermissionCodesForUser_distinctReturned() {
        SysPermission p1 = perm("orchard:read");
        SysPermission p2 = perm("orchard:write");
        SysPermission p3 = perm("orchard:read"); // duplicate from another role
        when(permissionMapper.findByUserId(100L)).thenReturn(List.of(p1, p2, p3));

        List<String> codes = service.getPermissionCodesForUser(100L);

        assertThat(codes).containsExactlyInAnyOrder("orchard:read", "orchard:write");
    }

    @Test
    @DisplayName("getPermissionCodesForUser - userId 为空返回空")
    void getPermissionCodesForUser_nullUserId_returnsEmpty() {
        assertThat(service.getPermissionCodesForUser(null)).isEmpty();
        verifyNoInteractions(permissionMapper);
    }

    @Test
    @DisplayName("assignRolesByCode - 先删后插，按角色编码插入")
    void assignRolesByCode_deletesThenInserts() {
        when(roleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(admin, farmer));

        service.assignRolesByCode(100L, List.of("ADMIN", "FARMER"), "tester");

        verify(userRoleMapper).deleteByUserId(100L);
        verify(userRoleMapper).insert(eq(100L), eq(1L), eq("tester"));
        verify(userRoleMapper).insert(eq(100L), eq(2L), eq("tester"));
    }

    @Test
    @DisplayName("assignRolesByCode - 未知角色编码抛 BizException")
    void assignRolesByCode_unknownCode_throws() {
        when(roleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(admin)); // FARMER missing

        assertThatThrownBy(() -> service.assignRolesByCode(100L, List.of("ADMIN", "FARMER"), "tester"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("未知");

        verify(userRoleMapper).deleteByUserId(100L);
        verify(userRoleMapper, never()).insert(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("assignRolesByCode - userId 为空抛 BizException")
    void assignRolesByCode_nullUserId_throws() {
        assertThatThrownBy(() -> service.assignRolesByCode(null, List.of("ADMIN"), "tester"))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("assignRoles - 仅删除（roleIds 为空）也是合法清空操作")
    void assignRoles_emptyList_clearsOnly() {
        service.assignRoles(100L, List.of(), "tester");

        verify(userRoleMapper).deleteByUserId(100L);
        verify(userRoleMapper, never()).insert(anyLong(), anyLong(), anyString());
    }

    @Test
    @DisplayName("getPermissionMatrix - 返回 roleCode -> permCodes")
    void getPermissionMatrix_returnsRoleToPermsMap() {
        when(roleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(admin, farmer));
        when(permissionMapper.findByRoleId(1L))
                .thenReturn(List.of(perm("orchard:read"), perm("orchard:write"), perm("user:read")));
        when(permissionMapper.findByRoleId(2L))
                .thenReturn(List.of(perm("orchard:read"), perm("orchard:write")));

        Map<String, List<String>> matrix = service.getPermissionMatrix();

        assertThat(matrix).containsOnlyKeys("ADMIN", "FARMER");
        assertThat(matrix.get("ADMIN")).containsExactly("orchard:read", "orchard:write", "user:read");
        assertThat(matrix.get("FARMER")).containsExactly("orchard:read", "orchard:write");
    }

    private SysPermission perm(String code) {
        SysPermission p = new SysPermission();
        p.setPermCode(code);
        return p;
    }
}
