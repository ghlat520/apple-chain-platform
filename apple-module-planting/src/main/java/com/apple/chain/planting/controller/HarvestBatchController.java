package com.apple.chain.planting.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.HarvestBatch;
import com.apple.chain.planting.service.HarvestBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Harvest batch endpoints.
 */
@Tag(name = "采收批次管理")
@RestController
@RequestMapping("/api/planting/harvest")
@RequiredArgsConstructor
public class HarvestBatchController {

    private final HarvestBatchService harvestBatchService;

    @Operation(summary = "采收批次列表（分页）")
    @GetMapping("/list")
    public R<PageResult<HarvestBatch>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long orchardId,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(harvestBatchService.listBatches(page, size, orchardId, status)));
    }

    @Operation(summary = "采收批次详情")
    @GetMapping("/{id}")
    public R<HarvestBatch> detail(@PathVariable Long id) {
        return R.ok(harvestBatchService.getBatchDetail(id));
    }

    @Operation(summary = "创建采收批次")
    @PostMapping
    public R<HarvestBatch> create(@RequestBody HarvestBatch batch) {
        return R.ok(harvestBatchService.createBatch(batch));
    }

    @Operation(summary = "更新采收批次")
    @PutMapping("/{id}")
    public R<HarvestBatch> update(@PathVariable Long id, @RequestBody HarvestBatch batch) {
        return R.ok(harvestBatchService.updateBatch(id, batch));
    }

    @Operation(summary = "确认采收批次（生成溯源码）")
    @PutMapping("/{id}/confirm")
    public R<HarvestBatch> confirm(@PathVariable Long id) {
        return R.ok(harvestBatchService.confirmBatch(id));
    }

    @Operation(summary = "删除采收批次（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        harvestBatchService.deleteBatch(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出采收批次CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) Long orchardId,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        harvestBatchService.exportBatches(orchardId, status, response);
    }
}
