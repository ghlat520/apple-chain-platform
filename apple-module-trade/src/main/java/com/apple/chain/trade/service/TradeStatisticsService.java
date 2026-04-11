package com.apple.chain.trade.service;

import com.apple.chain.trade.dto.TradeStatisticsVO;

import java.util.List;

/**
 * Trade statistics service (F-405).
 */
public interface TradeStatisticsService {

    /** Overall summary: totals + order counts by status. */
    TradeStatisticsVO getSummary();

    /** Volume / amount / avg-price breakdown per variety. */
    List<TradeStatisticsVO.VarietyStats> getVarietyBreakdown();

    /** Monthly volume / amount / order-count for the last {@code months} calendar months. */
    List<TradeStatisticsVO.MonthlyStats> getMonthlyTrend(int months);

    /**
     * Simple price index: average unit-price for the given variety over the last
     * {@code days} days, grouped by trade-date.
     *
     * @return list of {@link TradeStatisticsVO.MonthlyStats} with {@code month} set to
     *         the trade-date string and {@code avgPrice} set to the daily average.
     */
    List<TradeStatisticsVO.MonthlyStats> getPriceIndex(String variety, int days);
}
