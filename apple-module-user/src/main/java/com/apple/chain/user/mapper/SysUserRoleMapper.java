package com.apple.chain.user.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Direct SQL mapper for the sys_user_role association table
 * (no PK column, so no MyBatis-Plus BaseMapper).
 */
@Mapper
public interface SysUserRoleMapper {

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO sys_user_role(user_id, role_id, grant_by) VALUES(#{userId}, #{roleId}, #{grantBy})")
    int insert(@Param("userId") Long userId,
               @Param("roleId") Long roleId,
               @Param("grantBy") String grantBy);
}
