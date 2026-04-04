package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Harvest batch (采收批次) — records a single harvest event from an orchard.
 */
@Data
@TableName("harvest_batch")
public class HarvestBatch {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Auto-generated code like HB20240401001 */
    private String batchCode;

    private Long orchardId;

    private LocalDate harvestDate;

    /** Estimated weight in kg */
    private BigDecimal estimatedWeight;

    /** Actual weight in kg */
    private BigDecimal actualWeight;

    /** 一级/二级/三级/等外 */
    private String grade;

    /** pending / listed / sold */
    private String status;

    private String description;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
