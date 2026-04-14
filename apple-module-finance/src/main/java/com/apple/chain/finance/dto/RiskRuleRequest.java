package com.apple.chain.finance.dto;

import com.apple.chain.finance.enums.RiskSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Create / update payload for {@code RiskRule}.
 */
@Getter
@Setter
public class RiskRuleRequest {

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称最长100字")
    private String name;

    @NotBlank(message = "规则类型不能为空")
    @Size(max = 50, message = "规则类型最长50字")
    private String ruleType;

    @NotNull(message = "阈值不能为空")
    private BigDecimal threshold;

    @NotNull(message = "严重度不能为空")
    private RiskSeverity severity;

    /** Defaults to true on create when omitted. */
    private Boolean enabled;
}
