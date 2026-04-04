package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Trace record (溯源记录) — immutable record of each supply-chain stage.
 * hashValue = SHA-256 of (traceCode + stage + operationTime + description).
 */
@Data
@TableName("trace_record")
public class TraceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Unique QR / trace code — shared across all stages of the same batch */
    private String traceCode;

    private Long batchId;

    private Long orchardId;

    /** planting / harvest / quality_check / storage / transport / sale */
    private String stage;

    private String operatorName;

    private LocalDateTime operationTime;

    private String location;

    private String description;

    /** SHA-256 hex digest of key fields for tamper-evidence */
    private String hashValue;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
