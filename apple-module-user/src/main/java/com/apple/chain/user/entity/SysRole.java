package com.apple.chain.user.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * RBAC role definition. See V10__rbac_tables.sql.
 */
@Getter
@Setter
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String roleCode;
    private String roleName;
    private String description;
    private Integer sortOrder;
}
