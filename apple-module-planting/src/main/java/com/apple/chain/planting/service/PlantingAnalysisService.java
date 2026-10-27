package com.apple.chain.planting.service;

import com.apple.chain.planting.dto.PlantingAnalysisVO;

import java.util.List;

/**
 * Planting analysis service — yield ranking, premium rates, pest incidences, plot comparison.
 */
public interface PlantingAnalysisService {

    /**
     * Rank orchards by yield-per-mu for a given variety and year.
     *
     * @param variety apple variety filter (null = all)
     * @param year    plant year filter (null = all)
     * @return list sorted by yieldPerMu descending
     */
    List<PlantingAnalysisVO.YieldPerMu> getYieldRanking(String variety, Integer year);

    /**
     * Calculate Grade-A premium rates per orchard for a given variety and year.
     *
     * @param variety apple variety filter (null = all)
     * @param year    harvest year filter (null = all)
     * @return list sorted by premiumRate descending
     */
    List<PlantingAnalysisVO.PremiumRate> getPremiumRates(String variety, Integer year);

    /**
     * Calculate pest (PESTICIDE operation) incidence rates per orchard for a given year.
     *
     * @param year plant year filter (null = all)
     * @return list sorted by incidenceRate descending
     */
    List<PlantingAnalysisVO.PestIncidence> getPestIncidences(Integer year);

    /**
     * Combine all 3 metrics for the specified orchards.
     *
     * @param orchardIds list of orchard IDs to compare
     * @return one PlotComparison per requested orchard
     */
    List<PlantingAnalysisVO.PlotComparison> comparePlots(List<Long> orchardIds);
}
