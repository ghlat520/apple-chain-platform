package com.apple.chain.finance.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.apple.chain.finance.enums.RiskEventStatus;
import com.apple.chain.finance.enums.RiskSeverity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Risk warning event triggered by a {@link RiskRule}.
 *
 * <p>Persisted in {@code sf_risk_event} (see Flyway V28).
 */
@Getter
@Setter
@TableName("sf_risk_event")
public class RiskEvent extends BaseEntity {

    /** Originating rule. */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ruleId;

    /** Business object class, e.g. {@code LOAN}, {@code PLEDGE}, {@code FARMER}. */
    private String targetType;

    /** ID of the business object the event is about. */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    /** Severity (copied from rule at fire time, can be tuned manually). */
    private RiskSeverity severity;

    /** Numeric value that breached the rule threshold. */
    private BigDecimal triggerValue;

    /** Lifecycle status: PENDING → HANDLING → RESOLVED / IGNORED. */
    private RiskEventStatus status;

    /** ID of the user assigned to handle the event (nullable). */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assigneeId;

    /** Free-form remark left by the handler when closing the event. */
    private String handleRemark;

    /** When the event was triggered (set on insert). */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime triggerTime;

    /** When the event was last handled / closed. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime handleTime;
}
