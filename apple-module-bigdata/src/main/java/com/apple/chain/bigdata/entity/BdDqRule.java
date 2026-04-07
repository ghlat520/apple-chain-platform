package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data quality rule. Table: bd_dq_rule
 */
@Getter
@Setter
@TableName("bd_dq_rule")
public class BdDqRule extends BaseEntity {

    private String ruleCode;
    private String ruleName;
    private Long assetId;
    private String assetCode;
    private String fieldName;

    /** NOT_NULL / UNIQUE / RANGE / REGEX / ENUM / FRESHNESS */
    private String ruleType;

    private String ruleExpr;

    /** BLOCK / ERROR / WARN / INFO */
    private String severity;

    private Integer enabled;
    private String scheduleCron;
    private String owner;
    private LocalDateTime lastRunTime;
    private BigDecimal lastScore;
    private String remark;
}
