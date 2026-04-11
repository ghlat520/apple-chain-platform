package com.apple.chain.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class FinanceStatisticsVO {

    // Summary fields
    private Long totalLoans;
    private BigDecimal totalAmount;
    private Map<String, Long> loansByType;
    private Map<String, Long> loansByStatus;

    // Risk fields
    private Double overdueRate;
    private Map<String, Long> riskByLevel;
}
