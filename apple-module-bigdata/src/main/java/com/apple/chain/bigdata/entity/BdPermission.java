package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Permission entry. Table: bd_permission
 */
@Getter
@Setter
@TableName("bd_permission")
public class BdPermission extends BaseEntity {

    private String permCode;
    private String permName;
    private String module;

    /** MENU / BUTTON / API / DATA */
    private String permType;

    private String resourcePattern;
    private String parentCode;
    private Integer sortOrder;
}
