package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdAnalysisReport;
import com.apple.chain.bigdata.service.BdAnalysisReportService;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Analysis report controller.
 * Path: /api/bigdata/report
 */
@Tag(name = "分析报告")
@RestController
@RequestMapping("/api/bigdata/report")
@RequiredArgsConstructor
public class AnalysisReportController {

    private final BdAnalysisReportService analysisReportService;

    @Operation(summary = "生成分析报告")
    @PostMapping("/generate")
    public R<BdAnalysisReport> generate(
            @RequestParam String type,
            @RequestParam String period) {
        return R.ok(analysisReportService.generate(type, period));
    }

    @Operation(summary = "分页查询报告列表")
    @GetMapping
    public R<IPage<BdAnalysisReport>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(analysisReportService.list(page, size));
    }

    @Operation(summary = "查询报告详情")
    @GetMapping("/{id}")
    public R<BdAnalysisReport> detail(@PathVariable Long id) {
        return R.ok(analysisReportService.getDetail(id));
    }

    @Operation(summary = "发布报告")
    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        analysisReportService.publish(id);
        return R.ok();
    }
}
