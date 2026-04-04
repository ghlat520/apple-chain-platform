package com.apple.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * QR code record — links a traceCode to a harvest batch.
 */
@Data
@TableName("qr_code")
public class QrCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traceCode;

    private Long batchId;

    /** active / expired */
    private String status;

    private Integer scanCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
