package com.apple.chain.planting.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.MaturityRecord;
import com.apple.chain.planting.entity.MaturityStandard;
import com.apple.chain.planting.maturity.MaturityRecommendation;
import com.apple.chain.planting.service.MaturityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * M6 - Harvest time intelligent recommendation endpoints.
 *
 * <p>Base path: {@code /api/planting/maturity}.</p>
 */
@Tag(name = "成熟度与采收推荐 (M6)")
@RestController
@RequestMapping("/api/planting/maturity")
@RequiredArgsConstructor
public class MaturityController {

    private final MaturityService maturityService;

    @Operation(summary = "录入成熟度采样")
    @PostMapping("/record")
    public R<MaturityRecord> record(@RequestBody MaturityRecord record) {
        return R.ok(maturityService.recordMeasurement(record));
    }

    @Operation(summary = "获取最佳采收窗口推荐")
    @GetMapping("/recommend/{orchardId}")
    public R<MaturityRecommendation> recommend(@PathVariable Long orchardId) {
        return R.ok(maturityService.recommendHarvestWindow(orchardId));
    }

    @Operation(summary = "查询品种成熟度标准列表")
    @GetMapping("/standards")
    public R<List<MaturityStandard>> standards() {
        return R.ok(maturityService.listStandards());
    }

    @Operation(summary = "查询果园最近采样记录")
    @GetMapping("/records/{orchardId}")
    public R<List<MaturityRecord>> records(@PathVariable Long orchardId,
                                           @RequestParam(defaultValue = "20") int limit) {
        return R.ok(maturityService.listOrchardRecords(orchardId, limit));
    }
}
