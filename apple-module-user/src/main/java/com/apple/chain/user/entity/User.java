package com.apple.chain.user.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Platform user entity.
 * Table: uc_user
 */
@Getter
@Setter
@TableName("uc_user")
public class User extends BaseEntity {

    private String username;

    /** BCrypt hashed password. Never return to client. */
    private String password;

    private String realName;
    private String phone;
    private String email;

    /**
     * Role codes: FARMER / SUPPLIER / BUYER / WAREHOUSE / LOGISTICS / FINANCE / GOV / ADMIN
     */
    private String roleCode;

    /** 0=disabled, 1=active */
    private Integer status;

    private String orgName;
    private String orgType;
    private String avatar;
    private String remark;
}
