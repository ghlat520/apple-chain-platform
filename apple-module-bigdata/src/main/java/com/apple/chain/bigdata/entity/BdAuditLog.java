package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Big-data admin audit log. Table: bd_audit_log
 */
@Getter
@Setter
@TableName("bd_audit_log")
public class BdAuditLog extends BaseEntity {

    private Long userId;
    private String username;
    private String roleCode;
    private String module;
    private String action;
    private String targetType;
    private String targetId;
    private String requestIp;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private Integer responseStatus;
    private Long durationMs;
    private String errorMessage;
}
