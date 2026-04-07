package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Role-permission mapping. Table: bd_role_permission
 */
@Getter
@Setter
@TableName("bd_role_permission")
public class BdRolePermission extends BaseEntity {

    private String roleCode;
    private String permCode;
}
