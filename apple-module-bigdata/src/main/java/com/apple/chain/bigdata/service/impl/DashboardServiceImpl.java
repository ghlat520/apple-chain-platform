package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.mapper.DashboardMapper;
import com.apple.chain.bigdata.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard service implementation.
 * All statistics are queried from real database tables - no hardcoded values.
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrchards", dashboardMapper.countOrchards());
        stats.put("totalArea", dashboardMapper.sumOrchardArea());
        stats.put("totalHarvest", dashboardMapper.sumHarvestCurrentYear());
        stats.put("totalTrades", dashboardMapper.countCompletedTrades());
        stats.put("totalTradeAmount", dashboardMapper.sumTradeAmount());
        stats.put("activeFarmers", dashboardMapper.countActiveFarmers());
        stats.put("activeSupplies", dashboardMapper.countActiveSupplies());
        stats.put("pendingOrders", dashboardMapper.countPendingOrders());
        return stats;
    }

    @Override
    public List<Map<String, Object>> getHarvestTrend() {
        return dashboardMapper.harvestTrendLast6Months();
    }

    @Override
    public List<Map<String, Object>> getTradeTrend() {
        return dashboardMapper.tradeTrendLast6Months();
    }

    @Override
    public List<Map<String, Object>> getVarietyDistribution() {
        return dashboardMapper.varietyDistribution();
    }

    @Override
    public List<Map<String, Object>> getTopOrchards() {
        return dashboardMapper.topOrchardsByHarvest();
    }

    @Override
    public List<Map<String, Object>> getSupplyDemand() {
        return dashboardMapper.supplyDemandByVariety();
    }

    @Override
    public List<Map<String, Object>> getTraceStats() {
        return dashboardMapper.traceStatusDistribution();
    }
}
