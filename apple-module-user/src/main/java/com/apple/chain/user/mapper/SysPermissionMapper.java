package com.apple.chain.user.mapper;

import com.apple.chain.user.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** All distinct permissions granted to a user (via any of their roles). */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.perm_id = p.id " +
            "JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<SysPermission> findByUserId(@Param("userId") Long userId);

    /** Permissions for a single role (used by matrix endpoint). */
    @Select("SELECT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.perm_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<SysPermission> findByRoleId(@Param("roleId") Long roleId);
}
