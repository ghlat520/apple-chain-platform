package com.apple.chain.warehouse.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Warehouse management endpoints.
 */
@Tag(name = "仓库管理")
@RestController
@RequestMapping("/api/warehouse/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "仓库预警列表")
    @GetMapping("/alerts")
    public R<List<Warehouse>> alerts() {
        return R.ok(warehouseService.listAlerts());
    }

    @Operation(summary = "变更仓库状态")
    @PutMapping("/{id}/status")
    public R<Warehouse> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(warehouseService.changeStatus(id, body.get("status")));
    }

    @Operation(summary = "仓库列表（分页）")
    @GetMapping("/list")
    public R<PageResult<Warehouse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(warehouseService.listWarehouses(page, size, keyword, type, status)));
    }

    @Operation(summary = "仓库详情")
    @GetMapping("/{id}")
    public R<Warehouse> detail(@PathVariable Long id) {
        return R.ok(warehouseService.getWarehouseDetail(id));
    }

    @Operation(summary = "创建仓库")
    @PostMapping
    public R<Warehouse> create(@RequestBody Warehouse warehouse) {
        return R.ok(warehouseService.createWarehouse(warehouse));
    }

    @Operation(summary = "更新仓库")
    @PutMapping("/{id}")
    public R<Warehouse> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        return R.ok(warehouseService.updateWarehouse(id, warehouse));
    }

    @Operation(summary = "删除仓库")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出仓库CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        warehouseService.exportWarehouses(keyword, type, status, response);
    }
}
