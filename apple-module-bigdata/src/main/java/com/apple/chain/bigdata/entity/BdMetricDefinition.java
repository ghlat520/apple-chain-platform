package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Metric definition. Table: bd_metric_definition
 */
@Getter
@Setter
@TableName("bd_metric_definition")
public class BdMetricDefinition extends BaseEntity {

    private String metricCode;
    private String metricName;

    /** PLANTING / TRADE / WAREHOUSE / LOGISTICS / FINANCE / TRACE */
    private String category;

    private String unit;
    private String calcFormula;
    private String sqlExpression;
    private Long dataSourceId;
    private String refreshCron;
    private String owner;
    private Integer isCore;
    private String remark;
}
