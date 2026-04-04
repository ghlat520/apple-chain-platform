package com.apple.chain.trade.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.entity.TradeOrderMvp;
import com.apple.chain.trade.service.TradeOrderMvpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * MVP trade order controller.
 * Path: /api/trade/orders
 *
 * Uses the MVP trade_order table with PENDING/CONFIRMED/SHIPPED/COMPLETED/CANCELLED status
 * and UNPAID/PAID payment_status.
 */
@Tag(name = "收购交易管理(MVP)")
@RestController
@RequestMapping("/api/trade/orders")
@RequiredArgsConstructor
public class TradeOrderMvpController {

    private final TradeOrderMvpService tradeOrderMvpService;

    @Operation(summary = "订单列表（分页 + keyword + status + paymentStatus）")
    @GetMapping
    public R<PageResult<TradeOrderMvp>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus) {
        return R.ok(PageResult.of(tradeOrderMvpService.listOrders(page, size, keyword, status, paymentStatus)));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public R<TradeOrderMvp> detail(@PathVariable Long id) {
        return R.ok(tradeOrderMvpService.getOrderDetail(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public R<TradeOrderMvp> create(@RequestBody TradeOrderMvp order) {
        return R.ok(tradeOrderMvpService.createOrder(order));
    }

    @Operation(summary = "更新订单基础信息")
    @PutMapping("/{id}")
    public R<TradeOrderMvp> update(@PathVariable Long id, @RequestBody TradeOrderMvp order) {
        return R.ok(tradeOrderMvpService.updateOrder(id, order));
    }

    @Operation(summary = "更新订单状态 (status: CONFIRMED/SHIPPED/COMPLETED/CANCELLED)")
    @PutMapping("/{id}/status")
    public R<TradeOrderMvp> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return R.ok(tradeOrderMvpService.updateStatus(id, status));
    }

    @Operation(summary = "删除订单（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        tradeOrderMvpService.deleteOrder(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出订单CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            HttpServletResponse response) {
        tradeOrderMvpService.exportOrders(keyword, status, paymentStatus, response);
    }
}
