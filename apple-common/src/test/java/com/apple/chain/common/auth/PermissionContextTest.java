package com.apple.chain.common.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PermissionContext 单元测试")
class PermissionContextTest {

    @AfterEach
    void cleanup() {
        PermissionContext.clear();
    }

    @Test
    @DisplayName("set + getPermissions/getRoles 返回写入的内容")
    void set_storesValues() {
        PermissionContext.set(Set.of("ADMIN"), Set.of("orchard:read", "orchard:write"));

        assertThat(PermissionContext.getRoles()).containsExactly("ADMIN");
        assertThat(PermissionContext.getPermissions())
                .containsExactlyInAnyOrder("orchard:read", "orchard:write");
    }

    @Test
    @DisplayName("set 接受 null 入参，对外暴露空集合")
    void set_nullArgs_returnsEmpty() {
        PermissionContext.set(null, null);

        assertThat(PermissionContext.getRoles()).isEmpty();
        assertThat(PermissionContext.getPermissions()).isEmpty();
    }

    @Test
    @DisplayName("hasPermission - 已授权返回 true")
    void hasPermission_granted_returnsTrue() {
        PermissionContext.set(Set.of("FARMER"), Set.of("orchard:write"));

        assertThat(PermissionContext.hasPermission("orchard:write")).isTrue();
    }

    @Test
    @DisplayName("hasPermission - 未授权返回 false")
    void hasPermission_notGranted_returnsFalse() {
        PermissionContext.set(Set.of("FARMER"), Set.of("orchard:read"));

        assertThat(PermissionContext.hasPermission("orchard:write")).isFalse();
    }

    @Test
    @DisplayName("hasPermission - null/空字符串返回 true（视为不要求权限）")
    void hasPermission_nullOrEmpty_returnsTrue() {
        PermissionContext.set(Set.of(), Set.of());

        assertThat(PermissionContext.hasPermission(null)).isTrue();
        assertThat(PermissionContext.hasPermission("")).isTrue();
    }

    @Test
    @DisplayName("hasRole - 已分配返回 true")
    void hasRole_assigned_returnsTrue() {
        PermissionContext.set(Set.of("ADMIN", "FARMER"), Set.of());

        assertThat(PermissionContext.hasRole("ADMIN")).isTrue();
        assertThat(PermissionContext.hasRole("FARMER")).isTrue();
        assertThat(PermissionContext.hasRole("BUYER")).isFalse();
    }

    @Test
    @DisplayName("clear 后读取返回空集合")
    void clear_emptiesContext() {
        PermissionContext.set(Set.of("ADMIN"), Set.of("orchard:write"));
        PermissionContext.clear();

        assertThat(PermissionContext.getRoles()).isEmpty();
        assertThat(PermissionContext.getPermissions()).isEmpty();
        assertThat(PermissionContext.hasPermission("orchard:write")).isFalse();
    }

    @Test
    @DisplayName("PermissionContext 内部 snapshot 不可变 — 外部 mutate 入参集合不影响已存储状态")
    void set_storesImmutableCopy() {
        Set<String> mutable = new HashSet<>();
        mutable.add("orchard:read");
        PermissionContext.set(Set.of("FARMER"), mutable);

        // mutate the source set after storing
        mutable.add("trade:write");

        assertThat(PermissionContext.getPermissions())
                .containsExactly("orchard:read");
    }

    @Test
    @DisplayName("外部修改 getPermissions() 返回值应抛异常（不可变保护）")
    void getPermissions_isUnmodifiable() {
        PermissionContext.set(Set.of("ADMIN"), Set.of("orchard:read"));

        assertThatThrownBy(() -> PermissionContext.getPermissions().add("trade:write"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
