package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.mapper.StatisticsMapper;
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
 * Statistics dashboard controller.
 * Path: /api/stats
 *
 * All aggregations use real SQL COUNT/SUM/GROUP BY queries — no hardcoded data.
 */
@Tag(name = "数据统计")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsMapper statisticsMapper;

    /**
     * GET /api/stats/summary
     * Returns total orchards, batches, orders, users via real COUNT(*) queries.
     */
    @Operation(summary = "KPI汇总 - 果园/批次/订单/用户总数")
    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrchards",        statisticsMapper.countTotalOrchards());
        result.put("totalBatches",         statisticsMapper.countTotalBatches());
        result.put("totalOrders",          statisticsMapper.countTotalOrders());
        result.put("totalUsers",           statisticsMapper.countTotalUsers());
        result.put("activeOrchards",       statisticsMapper.countActiveOrchards());
        result.put("completedOrders",      statisticsMapper.countCompletedOrders());
        result.put("totalTradeAmount",     statisticsMapper.sumCompletedTradeAmount());
        result.put("totalProducts",        statisticsMapper.countTotalProducts());
        result.put("lowInventory",         statisticsMapper.countLowInventory());
        result.put("totalUsages",          statisticsMapper.countTotalUsages());
        result.put("totalWarehouses",      statisticsMapper.countTotalWarehouses());
        return R.ok(result);
    }

    /**
     * GET /api/stats/trend
     * Monthly order amounts for the last 6 months.
     * Uses DATE_FORMAT + GROUP BY MONTH.
     */
    @Operation(summary = "近6个月月度交易趋势（DATE_FORMAT GROUP BY）")
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend() {
        return R.ok(statisticsMapper.monthlyOrderTrendLast6Months());
    }

    /**
     * GET /api/stats/top-varieties
     * Top 5 apple varieties by order count using ORDER BY COUNT DESC LIMIT 5.
     */
    @Operation(summary = "TOP5苹果品种 - 按订单数排名")
    @GetMapping("/top-varieties")
    public R<List<Map<String, Object>>> topVarieties() {
        return R.ok(statisticsMapper.topVarietiesByOrderCount());
    }

    /**
     * GET /api/stats/pending
     * Pending review counts per module.
     */
    @Operation(summary = "各模块待处理事项统计")
    @GetMapping("/pending")
    public R<Map<String, Object>> pending() {
        Map<String, Object> result = new HashMap<>();
        result.put("pendingOrders",        statisticsMapper.countPendingOrders());
        result.put("unpaidOrders",         statisticsMapper.countUnpaidOrders());
        result.put("draftBatches",         statisticsMapper.countDraftBatches());
        result.put("inactiveFarmers",      statisticsMapper.countInactiveFarmers());
        return R.ok(result);
    }
}
