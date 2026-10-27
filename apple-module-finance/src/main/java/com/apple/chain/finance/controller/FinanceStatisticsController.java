package com.apple.chain.finance.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.finance.dto.FinanceStatisticsVO;
import com.apple.chain.finance.service.FinanceStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "金融统计")
@RestController
@RequestMapping("/api/finance/statistics")
@RequiredArgsConstructor
public class FinanceStatisticsController {

    private final FinanceStatisticsService financeStatisticsService;

    @Operation(summary = "贷款汇总统计（总数、总金额、按类型、按状态）")
    @GetMapping("/summary")
    public R<FinanceStatisticsVO> summary() {
        return R.ok(financeStatisticsService.getSummary());
    }

    @Operation(summary = "风险统计（逾期率、按信用等级分布）")
    @GetMapping("/risk")
    public R<FinanceStatisticsVO> risk() {
        return R.ok(financeStatisticsService.getRiskSummary());
    }
}
