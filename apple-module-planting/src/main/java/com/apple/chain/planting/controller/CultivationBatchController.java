package com.apple.chain.planting.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.CultivationBatch;
import com.apple.chain.planting.service.CultivationBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Cultivation batch management endpoints.
 * Path: /api/cultivation/batches
 *
 * Covers: CRUD, pagination with keyword/status/orchardId filtering, CSV export.
 */
@Tag(name = "种植批次管理")
@RestController
@RequestMapping("/api/cultivation/batches")
@RequiredArgsConstructor
public class CultivationBatchController {

    private final CultivationBatchService batchService;

    @Operation(summary = "种植批次列表（分页 + keyword + status + orchardId）")
    @GetMapping
    public R<PageResult<CultivationBatch>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orchardId) {
        return R.ok(PageResult.of(batchService.listBatches(page, size, keyword, status, orchardId)));
    }

    @Operation(summary = "批次详情")
    @GetMapping("/{id}")
    public R<CultivationBatch> detail(@PathVariable Long id) {
        return R.ok(batchService.getBatchDetail(id));
    }

    @Operation(summary = "创建种植批次（自动生成batchCode）")
    @PostMapping
    public R<CultivationBatch> create(@Valid @RequestBody CultivationBatch batch) {
        return R.ok(batchService.createBatch(batch));
    }

    @Operation(summary = "更新种植批次")
    @PutMapping("/{id}")
    public R<CultivationBatch> update(@PathVariable Long id, @RequestBody CultivationBatch batch) {
        return R.ok(batchService.updateBatch(id, batch));
    }

    @Operation(summary = "删除种植批次（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出种植批次CSV（UTF-8 BOM）")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orchardId,
            HttpServletResponse response) {
        batchService.exportBatches(keyword, status, orchardId, response);
    }
}
