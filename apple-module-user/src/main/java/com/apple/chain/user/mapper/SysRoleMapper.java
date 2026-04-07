package com.apple.chain.user.mapper;

import com.apple.chain.user.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** Roles assigned to a given user via sys_user_role. */
    @Select("SELECT r.* FROM sys_role r " +
            "JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 " +
            "ORDER BY r.sort_order ASC")
    List<SysRole> findByUserId(@Param("userId") Long userId);
}
