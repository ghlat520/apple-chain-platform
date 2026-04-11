package com.apple.chain.planting.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.planting.dto.PlantingAnalysisVO;
import com.apple.chain.planting.service.PlantingAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Planting analysis endpoints.
 */
@Tag(name = "种植分析")
@RestController
@RequestMapping("/api/planting/analysis")
@RequiredArgsConstructor
public class PlantingAnalysisController {

    private final PlantingAnalysisService plantingAnalysisService;

    @Operation(summary = "亩产排名")
    @GetMapping("/yield")
    public R<List<PlantingAnalysisVO.YieldPerMu>> yieldRanking(
            @RequestParam(required = false) String variety,
            @RequestParam(required = false) Integer year) {
        return R.ok(plantingAnalysisService.getYieldRanking(variety, year));
    }

    @Operation(summary = "优果率统计")
    @GetMapping("/premium")
    public R<List<PlantingAnalysisVO.PremiumRate>> premiumRates(
            @RequestParam(required = false) String variety,
            @RequestParam(required = false) Integer year) {
        return R.ok(plantingAnalysisService.getPremiumRates(variety, year));
    }

    @Operation(summary = "病虫害发生率")
    @GetMapping("/pest")
    public R<List<PlantingAnalysisVO.PestIncidence>> pestIncidences(
            @RequestParam(required = false) Integer year) {
        return R.ok(plantingAnalysisService.getPestIncidences(year));
    }

    @Operation(summary = "地块对比分析")
    @GetMapping("/compare")
    public R<List<PlantingAnalysisVO.PlotComparison>> comparePlots(
            @RequestParam List<Long> orchardIds) {
        return R.ok(plantingAnalysisService.comparePlots(orchardIds));
    }
}
