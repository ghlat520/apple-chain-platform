package com.apple.chain.trace.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trace.entity.TraceBatch;
import com.apple.chain.trace.entity.TraceRecord;
import com.apple.chain.trace.service.TraceBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TraceBatch endpoints.
 * Path: /api/trace/batches
 */
@Tag(name = "溯源批次管理")
@RestController
@RequestMapping("/api/trace/batches")
@RequiredArgsConstructor
public class TraceBatchController {

    private final TraceBatchService traceBatchService;

    @Operation(summary = "批次列表（分页）")
    @GetMapping
    public R<PageResult<TraceBatch>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(traceBatchService.listBatches(page, size, keyword, status)));
    }

    @Operation(summary = "批次详情")
    @GetMapping("/{id}")
    public R<TraceBatch> detail(@PathVariable Long id) {
        return R.ok(traceBatchService.getBatchDetail(id));
    }

    @Operation(summary = "公众扫码查询（无需登录）")
    @GetMapping("/scan/{batchCode}")
    public R<Map<String, Object>> scan(@PathVariable String batchCode) {
        return R.ok(traceBatchService.scanByBatchCode(batchCode));
    }

    @Operation(summary = "创建溯源批次（自动生成区块链哈希）")
    @PostMapping
    public R<TraceBatch> create(@Valid @RequestBody TraceBatch batch) {
        return R.ok(traceBatchService.createBatch(batch));
    }

    @Operation(summary = "更新批次状态")
    @PutMapping("/{id}/status")
    public R<TraceBatch> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return R.ok(traceBatchService.updateStatus(id, status));
    }

    @Operation(summary = "删除批次（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        traceBatchService.deleteBatch(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "添加溯源记录")
    @PostMapping("/{id}/records")
    public R<TraceRecord> addRecord(
            @PathVariable Long id,
            @RequestBody TraceRecord record) {
        return R.ok(traceBatchService.addRecord(id, record));
    }

    @Operation(summary = "查询批次溯源记录")
    @GetMapping("/{id}/records")
    public R<List<TraceRecord>> listRecords(@PathVariable Long id) {
        return R.ok(traceBatchService.listRecords(id));
    }

    @Operation(summary = "导出批次CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        traceBatchService.exportBatches(keyword, status, response);
    }
}
