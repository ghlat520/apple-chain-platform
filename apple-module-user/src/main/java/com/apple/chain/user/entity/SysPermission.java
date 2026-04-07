package com.apple.chain.user.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * RBAC permission point (resource:action). See V10__rbac_tables.sql.
 */
@Getter
@Setter
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private String permCode;
    private String resource;
    private String action;
    private String description;
}
