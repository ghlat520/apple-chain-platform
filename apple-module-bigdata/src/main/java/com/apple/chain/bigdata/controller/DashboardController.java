package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.service.DashboardService;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Dashboard / BigData statistics endpoints.
 * All data is sourced from real SQL aggregations on production tables.
 */
@Tag(name = "数据大屏")
@RestController
@RequestMapping("/api/bigdata/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "KPI汇总统计")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(dashboardService.getStats());
    }

    @Operation(summary = "近6个月采收趋势（按月）")
    @GetMapping("/harvest-trend")
    public R<List<Map<String, Object>>> harvestTrend() {
        return R.ok(dashboardService.getHarvestTrend());
    }

    @Operation(summary = "近6个月交易趋势（按月）")
    @GetMapping("/trade-trend")
    public R<List<Map<String, Object>>> tradeTrend() {
        return R.ok(dashboardService.getTradeTrend());
    }

    @Operation(summary = "苹果品种分布（按采收量）")
    @GetMapping("/variety-distribution")
    public R<List<Map<String, Object>>> varietyDistribution() {
        return R.ok(dashboardService.getVarietyDistribution());
    }

    @Operation(summary = "今年采收量TOP5果园")
    @GetMapping("/top-orchards")
    public R<List<Map<String, Object>>> topOrchards() {
        return R.ok(dashboardService.getTopOrchards());
    }

    @Operation(summary = "供需对比（按品种）")
    @GetMapping("/supply-demand")
    public R<List<Map<String, Object>>> supplyDemand() {
        return R.ok(dashboardService.getSupplyDemand());
    }

    @Operation(summary = "溯源链状态分布")
    @GetMapping("/trace-stats")
    public R<List<Map<String, Object>>> traceStats() {
        return R.ok(dashboardService.getTraceStats());
    }
}
