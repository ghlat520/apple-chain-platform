package com.apple.chain.planting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * View objects for planting analysis endpoints.
 */
public class PlantingAnalysisVO {

    /**
     * Per-mu yield ranking for an orchard.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YieldPerMu {
        private Long orchardId;
        private String orchardName;
        private String variety;
        /** Area in mu */
        private BigDecimal areaMu;
        /** Total actual yield in kg */
        private BigDecimal totalYield;
        /** yield per mu = totalYield / areaMu */
        private BigDecimal yieldPerMu;
    }

    /**
     * Premium (Grade A) rate for an orchard.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PremiumRate {
        private Long orchardId;
        private String orchardName;
        /** Total Grade A weight in kg */
        private BigDecimal gradeAWeight;
        /** Total weight in kg */
        private BigDecimal totalWeight;
        /** premiumRate = gradeAWeight / totalWeight */
        private BigDecimal premiumRate;
    }

    /**
     * Pest incidence rate for an orchard.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PestIncidence {
        private Long orchardId;
        private String orchardName;
        /** Total cultivation operations count */
        private Long totalOperations;
        /** Pesticide operations count */
        private Long pestOperations;
        /** incidenceRate = pestOperations / totalOperations */
        private BigDecimal incidenceRate;
    }

    /**
     * Combined metrics for a single orchard (used by comparePlots).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlotComparison {
        private Long orchardId;
        private String orchardName;
        private YieldPerMu yieldMetrics;
        private PremiumRate premiumMetrics;
        private PestIncidence pestMetrics;
    }
}
