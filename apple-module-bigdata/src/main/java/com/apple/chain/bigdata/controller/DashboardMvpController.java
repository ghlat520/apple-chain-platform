package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.mapper.DashboardMvpMapper;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MVP Dashboard controller.
 * Path: /api/dashboard
 *
 * All data sourced from real SQL aggregations on MVP tables.
 */
@Tag(name = "数据看板(MVP)")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardMvpController {

    private final DashboardMvpMapper dashboardMvpMapper;

    @Operation(summary = "KPI汇总统计 - 果园总数/批次总数/交易总额/待处理订单数")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrchards", dashboardMvpMapper.countActiveOrchards());
        stats.put("totalBatches", dashboardMvpMapper.countTotalBatches());
        stats.put("totalTradeAmount", dashboardMvpMapper.sumCompletedTradeAmount());
        stats.put("pendingOrders", dashboardMvpMapper.countPendingOrders());
        return R.ok(stats);
    }

    @Operation(summary = "近6个月交易趋势（按月 GROUP BY）")
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend() {
        return R.ok(dashboardMvpMapper.tradeTrendLast6Months());
    }

    @Operation(summary = "TOP5果园交易量排名")
    @GetMapping("/top-orchards")
    public R<List<Map<String, Object>>> topOrchards() {
        return R.ok(dashboardMvpMapper.topOrchardsByTradeVolume());
    }

    @Operation(summary = "待处理事项统计 - 按类型分组计数")
    @GetMapping("/pending-actions")
    public R<List<Map<String, Object>>> pendingActions() {
        return R.ok(dashboardMvpMapper.pendingActionsCounts());
    }
}
