package com.apple.chain.warehouse.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.warehouse.dto.WarehouseStatisticsVO;
import com.apple.chain.warehouse.service.WarehouseStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仓储统计")
@RestController
@RequestMapping("/api/warehouse/statistics")
@RequiredArgsConstructor
public class WarehouseStatisticsController {

    private final WarehouseStatisticsService statisticsService;

    @Operation(summary = "仓储概览统计")
    @GetMapping("/summary")
    public R<WarehouseStatisticsVO> summary() {
        return R.ok(statisticsService.getSummary());
    }

    @Operation(summary = "周转率统计")
    @GetMapping("/turnover")
    public R<WarehouseStatisticsVO.TurnoverVO> turnover(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "30") int days) {
        return R.ok(statisticsService.getTurnover(warehouseId, days));
    }

    @Operation(summary = "损耗率统计")
    @GetMapping("/loss")
    public R<WarehouseStatisticsVO.LossVO> loss(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "30") int days) {
        return R.ok(statisticsService.getLoss(warehouseId, days));
    }
}
