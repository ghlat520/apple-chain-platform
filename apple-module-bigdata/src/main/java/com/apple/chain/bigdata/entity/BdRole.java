package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Big-data role dictionary. Table: bd_role
 */
@Getter
@Setter
@TableName("bd_role")
public class BdRole extends BaseEntity {

    private String roleCode;
    private String roleName;
    private String description;
    private Integer sortOrder;
    private Integer enabled;
}
