package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Audit log for CUD operations across all entities.
 */
@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String entityType;

    private Long entityId;

    private String actionType;

    private Long operatorId;

    private String operatorIp;

    private String beforeJson;

    private String afterJson;

    private LocalDateTime operatedAt;
}
