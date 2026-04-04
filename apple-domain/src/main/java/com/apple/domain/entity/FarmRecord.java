package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Farm record (农事记录) — tracks cultivation operations per orchard.
 */
@Data
@TableName("farm_record")
public class FarmRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orchardId;

    /** 施肥/用药/修剪/灌溉/采收 */
    private String recordType;

    private LocalDate operationDate;

    private String operatorName;

    private String description;

    private String productName;

    private String productAmount;

    private String unit;

    /** Comma-separated image URLs */
    private String imageUrls;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
