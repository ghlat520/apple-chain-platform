package com.apple.chain.trade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Trade statistics view object (F-405).
 */
@Data
public class TradeStatisticsVO {

    private long totalOrders;
    private BigDecimal totalVolume;
    private BigDecimal totalAmount;
    private BigDecimal avgUnitPrice;

    /** order count keyed by orderStatus */
    private Map<String, Long> ordersByStatus;

    private List<VarietyStats> varietyBreakdown;
    private List<MonthlyStats> monthlyTrend;

    @Data
    public static class VarietyStats {
        private String variety;
        private BigDecimal volume;
        private BigDecimal amount;
        private BigDecimal avgPrice;
    }

    @Data
    public static class MonthlyStats {
        private String month;
        private BigDecimal volume;
        private BigDecimal amount;
        private long orderCount;
    }
}
