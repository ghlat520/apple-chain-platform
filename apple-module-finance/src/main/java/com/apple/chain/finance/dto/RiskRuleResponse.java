package com.apple.chain.finance.dto;

import com.apple.chain.finance.entity.RiskRule;
import com.apple.chain.finance.enums.RiskSeverity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API response shape for {@link RiskRule}. Mirrors the entity 1:1 today but is
 * decoupled so future fields (e.g. matching scope, notification channel) can be
 * added without leaking persistence details.
 */
@Getter
@Setter
public class RiskRuleResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;
    private String ruleType;
    private BigDecimal threshold;
    private RiskSeverity severity;
    private Boolean enabled;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long creatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    public static RiskRuleResponse from(RiskRule rule) {
        if (rule == null) return null;
        RiskRuleResponse r = new RiskRuleResponse();
        r.setId(rule.getId());
        r.setName(rule.getName());
        r.setRuleType(rule.getRuleType());
        r.setThreshold(rule.getThreshold());
        r.setSeverity(rule.getSeverity());
        r.setEnabled(rule.getEnabled());
        r.setCreatorId(rule.getCreatorId());
        r.setCreateTime(rule.getCreateTime());
        r.setUpdateTime(rule.getUpdateTime());
        return r;
    }
}
