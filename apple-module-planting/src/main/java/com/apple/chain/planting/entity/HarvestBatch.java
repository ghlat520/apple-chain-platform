package com.apple.chain.planting.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Harvest batch entity.
 * Table: pt_harvest_batch
 */
@Getter
@Setter
@TableName("pt_harvest_batch")
public class HarvestBatch extends BaseEntity {

    /** Unique batch number, format: HB+yyyyMMdd+seq */
    private String batchNo;

    private Long orchardId;
    private LocalDate harvestDate;

    /** Total weight in kg */
    private BigDecimal totalWeight;

    /** Grade A weight in kg */
    private BigDecimal gradeA;

    /** Grade B weight in kg */
    private BigDecimal gradeB;

    /** Grade C weight in kg */
    private BigDecimal gradeC;

    /** DRAFT / CONFIRMED / IN_STORAGE */
    private String status;

    /** Linked traceability code (set when confirmed) */
    private String traceCode;

    private String remark;
}
