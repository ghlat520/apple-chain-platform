package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Metric value snapshot. Table: bd_metric_value
 */
@Getter
@Setter
@TableName("bd_metric_value")
public class BdMetricValue extends BaseEntity {

    private String metricCode;
    private LocalDate statDate;

    /** DAY / WEEK / MONTH / YEAR */
    private String statPeriod;

    private BigDecimal metricValue;
    private String dimJson;
}
