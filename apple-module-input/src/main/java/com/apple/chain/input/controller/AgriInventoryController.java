package com.apple.chain.input.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.input.entity.AgriInventory;
import com.apple.chain.input.service.AgriInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Agricultural input inventory endpoints.
 */
@Tag(name = "农资库存")
@RestController
@RequestMapping("/api/input/inventory")
@RequiredArgsConstructor
public class AgriInventoryController {

    private final AgriInventoryService agriInventoryService;

    @Operation(summary = "库存列表（分页）")
    @GetMapping("/list")
    public R<PageResult<AgriInventory>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId) {
        return R.ok(PageResult.of(agriInventoryService.listInventory(page, size, keyword, status, farmerId)));
    }

    @Operation(summary = "库存详情")
    @GetMapping("/{id}")
    public R<AgriInventory> detail(@PathVariable Long id) {
        return R.ok(agriInventoryService.getDetail(id));
    }

    @Operation(summary = "创建库存记录")
    @PostMapping
    public R<AgriInventory> create(@RequestBody AgriInventory inventory) {
        return R.ok(agriInventoryService.createInventory(inventory));
    }

    @Operation(summary = "更新库存记录")
    @PutMapping("/{id}")
    public R<AgriInventory> update(@PathVariable Long id, @RequestBody AgriInventory inventory) {
        return R.ok(agriInventoryService.updateInventory(id, inventory));
    }

    @Operation(summary = "删除库存记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        agriInventoryService.deleteInventory(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "调整库存数量（delta正增负减）")
    @PostMapping("/{id}/adjust")
    public R<AgriInventory> adjust(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal delta = new BigDecimal(body.get("delta").toString());
        String reason = body.containsKey("reason") ? body.get("reason").toString() : null;
        return R.ok(agriInventoryService.adjustStock(id, delta, reason));
    }

    @Operation(summary = "库存预警列表（LOW/EMPTY）")
    @GetMapping("/alerts")
    public R<List<AgriInventory>> alerts() {
        return R.ok(agriInventoryService.listAlerts());
    }

    @Operation(summary = "导出库存CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            HttpServletResponse response) {
        agriInventoryService.exportInventory(keyword, status, farmerId, response);
    }
}
