package com.apple.chain.trace.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trace.entity.AnomalyTrace;
import com.apple.chain.trace.service.AnomalyTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "异常追溯")
@RestController
@RequestMapping("/api/trace/anomaly")
@RequiredArgsConstructor
public class AnomalyTraceController {

    private final AnomalyTraceService anomalyTraceService;

    @Operation(summary = "上报异常")
    @PostMapping
    public R<AnomalyTrace> report(@RequestBody AnomalyTrace anomaly) {
        return R.ok(anomalyTraceService.reportAnomaly(anomaly));
    }

    @Operation(summary = "异常列表")
    @GetMapping
    public R<PageResult<AnomalyTrace>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        return R.ok(PageResult.of(anomalyTraceService.listAnomalies(page, size, type, status, severity)));
    }

    @Operation(summary = "异常详情")
    @GetMapping("/{id}")
    public R<AnomalyTrace> detail(@PathVariable Long id) {
        return R.ok(anomalyTraceService.getAnomalyDetail(id));
    }

    @Operation(summary = "开始调查")
    @PutMapping("/{id}/investigate")
    public R<AnomalyTrace> investigate(@PathVariable Long id) {
        return R.ok(anomalyTraceService.investigate(id));
    }

    @Operation(summary = "解决异常")
    @PutMapping("/{id}/resolve")
    public R<AnomalyTrace> resolve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(anomalyTraceService.resolve(id, body.get("rootCause"), body.get("resolvedBy")));
    }

    @Operation(summary = "影响分析")
    @GetMapping("/impact/{traceCode}")
    public R<Map<String, Object>> impact(@PathVariable String traceCode) {
        return R.ok(anomalyTraceService.impactAnalysis(traceCode));
    }
}
