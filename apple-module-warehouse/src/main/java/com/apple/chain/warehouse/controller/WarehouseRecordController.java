package com.apple.chain.warehouse.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.warehouse.entity.WarehouseRecord;
import com.apple.chain.warehouse.service.WarehouseRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Warehouse inbound/outbound record endpoints.
 */
@Tag(name = "出入库记录")
@RestController
@RequestMapping("/api/warehouse/records")
@RequiredArgsConstructor
public class WarehouseRecordController {

    private final WarehouseRecordService warehouseRecordService;

    @Operation(summary = "出入库记录列表（分页）")
    @GetMapping("/list")
    public R<PageResult<WarehouseRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) Long warehouseId) {
        return R.ok(PageResult.of(warehouseRecordService.listRecords(page, size, keyword, recordType, warehouseId)));
    }

    @Operation(summary = "出入库记录详情")
    @GetMapping("/{id}")
    public R<WarehouseRecord> detail(@PathVariable Long id) {
        return R.ok(warehouseRecordService.getRecordDetail(id));
    }

    @Operation(summary = "创建出入库记录")
    @PostMapping
    public R<WarehouseRecord> create(@RequestBody WarehouseRecord record) {
        return R.ok(warehouseRecordService.createRecord(record));
    }

    @Operation(summary = "删除出入库记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        warehouseRecordService.deleteRecord(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出出入库记录CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String recordType,
            @RequestParam(required = false) Long warehouseId,
            HttpServletResponse response) {
        warehouseRecordService.exportRecords(keyword, recordType, warehouseId, response);
    }
}
