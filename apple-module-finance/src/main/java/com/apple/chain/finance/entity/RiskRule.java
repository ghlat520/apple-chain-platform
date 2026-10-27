package com.apple.chain.finance.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.apple.chain.finance.enums.RiskSeverity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Risk warning rule definition.
 *
 * <p>Inherits {@code id / createTime / updateTime / createBy / deleted} from
 * {@link BaseEntity}. Combined with {@code creatorId} below it satisfies the
 * task's audit-fields requirement (creator + create/update timestamps).
 *
 * <p>Persisted in {@code sf_risk_rule} (see Flyway V28).
 */
@Getter
@Setter
@TableName("sf_risk_rule")
public class RiskRule extends BaseEntity {

    /** Human-readable rule name, e.g. "贷款逾期超过30天". */
    private String name;

    /**
     * Free-form rule type code, e.g. {@code OVERDUE_DAYS},
     * {@code PRICE_DROP_PCT}, {@code QUALITY_FAIL_RATE},
     * {@code CONCENTRATION_RATIO}, {@code FRAUD_SCORE}.
     */
    private String ruleType;

    /** Numeric threshold; semantic meaning depends on {@link #ruleType}. */
    private BigDecimal threshold;

    /** Severity tier emitted by this rule. */
    private RiskSeverity severity;

    /** Whether the rule is enabled (1=on, 0=off). */
    private Boolean enabled;

    /** ID of the user who created the rule. */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorId;
}
