package com.apple.chain.bigdata.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Dashboard mapper - all queries are real SQL aggregations, no hardcoded values.
 */
@Mapper
public interface DashboardMapper {

    /** Total active orchards */
    long countOrchards();

    /** Total orchard area in mu */
    BigDecimal sumOrchardArea();

    /** Total harvest weight (kg) for current year */
    BigDecimal sumHarvestCurrentYear();

    /** Total completed trade orders count */
    long countCompletedTrades();

    /** Total completed trade order amount (yuan) */
    BigDecimal sumTradeAmount();

    /** Distinct active farmers count */
    long countActiveFarmers();

    /** Published supply listings count */
    long countActiveSupplies();

    /** Orders in DRAFT or CONFIRMED status */
    long countPendingOrders();

    /** Harvest trend: last 6 months total weight grouped by month */
    List<Map<String, Object>> harvestTrendLast6Months();

    /** Trade trend: last 6 months total amount grouped by month */
    List<Map<String, Object>> tradeTrendLast6Months();

    /** Apple variety distribution: variety -> total harvest weight */
    List<Map<String, Object>> varietyDistribution();

    /** Top 5 orchards by total harvest weight this year */
    List<Map<String, Object>> topOrchardsByHarvest();

    /** Supply vs demand quantity by variety */
    List<Map<String, Object>> supplyDemandByVariety();

    /** Trace chain status distribution */
    List<Map<String, Object>> traceStatusDistribution();
}
