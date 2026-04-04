package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cultivation batch entity.
 * Table: cultivation_batch
 *
 * Tracks a single apple production batch from planting through harvest.
 * Status: PLANTING / GROWING / READY_FOR_HARVEST / HARVESTED / CLOSED
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cultivation_batch")
public class CultivationBatch extends BaseEntity {

    /**
     * Auto-generated batch code: CB + yyyyMMdd + 4-digit seq.
     */
    private String batchCode;

    /**
     * FK farm_orchard.id
     */
    @NotNull(message = "果园ID不能为空")
    private Long orchardId;

    /**
     * Orchard name (denormalized for read performance).
     */
    private String orchardName;

    /**
     * Apple variety, e.g. 红富士 / 嘎拉 / 黄元帅 / 秦冠
     */
    @NotBlank(message = "苹果品种不能为空")
    private String appleVariety;

    /**
     * Year in which this batch was planted.
     */
    private Integer plantYear;

    /**
     * Expected yield in kg.
     */
    private BigDecimal expectedYield;

    /**
     * Actual yield in kg (filled after harvest).
     */
    private BigDecimal actualYield;

    /**
     * Planned/actual harvest date.
     */
    private LocalDate harvestDate;

    /**
     * PLANTING / GROWING / READY_FOR_HARVEST / HARVESTED / CLOSED
     */
    private String status;

    /**
     * Optional remark.
     */
    private String remark;
}
