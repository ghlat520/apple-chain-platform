package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Cultivation operation entity.
 * Table: cultivation_operation
 *
 * Logs individual farming operations on a cultivation batch.
 * OperationType: FERTILIZE / PESTICIDE / PRUNE / HARVEST / IRRIGATE / OTHER
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cultivation_operation")
public class CultivationOperation extends BaseEntity {

    /**
     * FK cultivation_batch.id
     */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    /**
     * Batch code (denormalized).
     */
    private String batchCode;

    /**
     * FERTILIZE / PESTICIDE / PRUNE / HARVEST / IRRIGATE / OTHER
     */
    @NotNull(message = "操作类型不能为空")
    private String operationType;

    /**
     * Date when the operation was performed.
     */
    private LocalDate operationDate;

    /**
     * Person who performed the operation.
     */
    private String operator;

    /**
     * Material inputs (JSON, e.g. fertilizer or pesticide name/amount).
     */
    private String materials;

    /**
     * Optional remark.
     */
    private String remark;
}
