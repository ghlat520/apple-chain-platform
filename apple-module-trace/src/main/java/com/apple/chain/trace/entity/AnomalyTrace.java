package com.apple.chain.trace.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("tr_anomaly_trace")
public class AnomalyTrace extends BaseEntity {

    private String traceCode;
    private Long batchId;

    /** PESTICIDE_EXCESS / TEMP_VIOLATION / QUALITY_FAIL / OTHER */
    private String anomalyType;

    private String description;

    /** HIGH / MEDIUM / LOW */
    private String severity;

    /** OPEN / INVESTIGATING / RESOLVED */
    private String status;

    private Integer affectedBatchCount;
    private Integer affectedFruitCount;

    private String rootCauseAnalysis;
    private String resolvedBy;
    private LocalDateTime resolvedTime;
    private String remark;
}
