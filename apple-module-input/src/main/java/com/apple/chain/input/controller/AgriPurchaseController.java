package com.apple.chain.input.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.input.entity.AgriPurchase;
import com.apple.chain.input.service.AgriPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agricultural input purchase endpoints.
 */
@Tag(name = "农资采购")
@RestController
@RequestMapping("/api/input/purchases")
@RequiredArgsConstructor
public class AgriPurchaseController {

    private final AgriPurchaseService agriPurchaseService;

    @Operation(summary = "采购列表（分页）")
    @GetMapping("/list")
    public R<PageResult<AgriPurchase>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId) {
        return R.ok(PageResult.of(agriPurchaseService.listPurchases(page, size, keyword, status, farmerId)));
    }

    @Operation(summary = "采购详情")
    @GetMapping("/{id}")
    public R<AgriPurchase> detail(@PathVariable Long id) {
        return R.ok(agriPurchaseService.getDetail(id));
    }

    @Operation(summary = "创建采购单")
    @PostMapping
    public R<AgriPurchase> create(@RequestBody AgriPurchase purchase) {
        return R.ok(agriPurchaseService.createPurchase(purchase));
    }

    @Operation(summary = "更新采购单")
    @PutMapping("/{id}")
    public R<AgriPurchase> update(@PathVariable Long id, @RequestBody AgriPurchase purchase) {
        return R.ok(agriPurchaseService.updatePurchase(id, purchase));
    }

    @Operation(summary = "删除采购单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        agriPurchaseService.deletePurchase(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "审批采购单（PENDING→APPROVED）")
    @PutMapping("/{id}/approve")
    public R<AgriPurchase> approve(@PathVariable Long id) {
        return R.ok(agriPurchaseService.approvePurchase(id));
    }

    @Operation(summary = "确认收货（APPROVED→RECEIVED，自动更新库存）")
    @PutMapping("/{id}/receive")
    public R<AgriPurchase> receive(@PathVariable Long id) {
        return R.ok(agriPurchaseService.receivePurchase(id));
    }

    @Operation(summary = "取消采购单（PENDING→CANCELLED）")
    @PutMapping("/{id}/cancel")
    public R<AgriPurchase> cancel(@PathVariable Long id) {
        return R.ok(agriPurchaseService.cancelPurchase(id));
    }

    @Operation(summary = "导出采购记录CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            HttpServletResponse response) {
        agriPurchaseService.exportPurchases(keyword, status, farmerId, response);
    }
}
