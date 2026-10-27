package com.apple.chain.trade.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.trade.dto.TradeStatisticsVO;
import com.apple.chain.trade.service.TradeStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Trade statistics endpoints (F-405).
 */
@Tag(name = "交易统计")
@RestController
@RequestMapping("/api/trade/statistics")
@RequiredArgsConstructor
public class TradeStatisticsController {

    private final TradeStatisticsService tradeStatisticsService;

    @Operation(summary = "交易汇总统计")
    @GetMapping("/summary")
    public R<TradeStatisticsVO> summary() {
        return R.ok(tradeStatisticsService.getSummary());
    }

    @Operation(summary = "品种维度统计")
    @GetMapping("/variety")
    public R<List<TradeStatisticsVO.VarietyStats>> variety() {
        return R.ok(tradeStatisticsService.getVarietyBreakdown());
    }

    @Operation(summary = "月度趋势统计")
    @GetMapping("/trend")
    public R<List<TradeStatisticsVO.MonthlyStats>> trend(
            @RequestParam(defaultValue = "6") int months) {
        return R.ok(tradeStatisticsService.getMonthlyTrend(months));
    }

    @Operation(summary = "品种价格指数")
    @GetMapping("/price-index")
    public R<List<TradeStatisticsVO.MonthlyStats>> priceIndex(
            @RequestParam String variety,
            @RequestParam(defaultValue = "30") int days) {
        return R.ok(tradeStatisticsService.getPriceIndex(variety, days));
    }
}
