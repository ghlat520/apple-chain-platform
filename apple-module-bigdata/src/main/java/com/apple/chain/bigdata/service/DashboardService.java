package com.apple.chain.bigdata.service;

import java.util.List;
import java.util.Map;

/**
 * Dashboard service interface - all data sourced from real DB aggregations.
 */
public interface DashboardService {

    Map<String, Object> getStats();

    List<Map<String, Object>> getHarvestTrend();

    List<Map<String, Object>> getTradeTrend();

    List<Map<String, Object>> getVarietyDistribution();

    List<Map<String, Object>> getTopOrchards();

    List<Map<String, Object>> getSupplyDemand();

    List<Map<String, Object>> getTraceStats();
}
