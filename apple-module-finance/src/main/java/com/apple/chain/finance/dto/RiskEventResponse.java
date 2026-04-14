package com.apple.chain.finance.dto;

import com.apple.chain.finance.entity.RiskEvent;
import com.apple.chain.finance.enums.RiskEventStatus;
import com.apple.chain.finance.enums.RiskSeverity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** API response shape for {@link RiskEvent}. */
@Getter
@Setter
public class RiskEventResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long ruleId;

    private String targetType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    private RiskSeverity severity;
    private BigDecimal triggerValue;
    private RiskEventStatus status;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assigneeId;

    private String handleRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime triggerTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime handleTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    public static RiskEventResponse from(RiskEvent event) {
        if (event == null) return null;
        RiskEventResponse r = new RiskEventResponse();
        r.setId(event.getId());
        r.setRuleId(event.getRuleId());
        r.setTargetType(event.getTargetType());
        r.setTargetId(event.getTargetId());
        r.setSeverity(event.getSeverity());
        r.setTriggerValue(event.getTriggerValue());
        r.setStatus(event.getStatus());
        r.setAssigneeId(event.getAssigneeId());
        r.setHandleRemark(event.getHandleRemark());
        r.setTriggerTime(event.getTriggerTime());
        r.setHandleTime(event.getHandleTime());
        r.setCreateTime(event.getCreateTime());
        return r;
    }
}
