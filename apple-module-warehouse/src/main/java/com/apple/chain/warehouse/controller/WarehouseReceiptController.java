package com.apple.chain.warehouse.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.warehouse.entity.WarehouseReceipt;
import com.apple.chain.warehouse.service.WarehouseReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Warehouse receipt (仓单) endpoints.
 */
@Tag(name = "智能仓单")
@RestController
@RequestMapping("/api/warehouse/receipts")
@RequiredArgsConstructor
public class WarehouseReceiptController {

    private final WarehouseReceiptService warehouseReceiptService;

    @Operation(summary = "仓单列表（分页）")
    @GetMapping("/list")
    public R<PageResult<WarehouseReceipt>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(warehouseReceiptService.listReceipts(page, size, keyword, status)));
    }

    @Operation(summary = "仓单详情")
    @GetMapping("/{id}")
    public R<WarehouseReceipt> detail(@PathVariable Long id) {
        return R.ok(warehouseReceiptService.getReceiptDetail(id));
    }

    @Operation(summary = "创建仓单")
    @PostMapping
    public R<WarehouseReceipt> create(@RequestBody WarehouseReceipt receipt) {
        return R.ok(warehouseReceiptService.createReceipt(receipt));
    }

    @Operation(summary = "更新仓单")
    @PutMapping("/{id}")
    public R<WarehouseReceipt> update(@PathVariable Long id, @RequestBody WarehouseReceipt receipt) {
        return R.ok(warehouseReceiptService.updateReceipt(id, receipt));
    }

    @Operation(summary = "变更仓单状态")
    @PutMapping("/{id}/status")
    public R<WarehouseReceipt> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return R.ok(warehouseReceiptService.changeStatus(id, status));
    }

    @Operation(summary = "删除仓单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        warehouseReceiptService.deleteReceipt(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出仓单CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        warehouseReceiptService.exportReceipts(keyword, status, response);
    }
}
