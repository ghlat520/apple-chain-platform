package com.apple.chain.trace.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Trace batch entity for harvest-level traceability.
 * Table: trace_batch
 *
 * Status flow: CREATED → PROCESSING → COMPLETED → SHIPPED
 * Grade: A / B / C
 */
@Getter
@Setter
@TableName("trace_batch")
public class TraceBatch extends BaseEntity {

    /** Auto-generated batch code: TB + yyyyMMdd + 4-digit seq */
    private String batchCode;

    @NotNull(message = "果园ID不能为空")
    private Long orchardId;

    private String orchardName;

    @NotNull(message = "采收日期不能为空")
    private LocalDate harvestDate;

    private String variety;

    /** A / B / C */
    private String grade;

    /** Total weight in kg */
    private BigDecimal weight;

    /** CREATED / PROCESSING / COMPLETED / SHIPPED */
    private String status;

    /**
     * SHA-256 hex hash of (batchCode + orchardId + harvestDate + variety + weight).
     * Computed on creation and stored for integrity verification.
     */
    private String blockchainHash;
}
