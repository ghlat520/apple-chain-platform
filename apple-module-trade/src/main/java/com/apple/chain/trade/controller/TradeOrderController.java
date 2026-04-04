package com.apple.chain.trade.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.service.TradeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Trade order endpoints.
 */
@Tag(name = "交易订单管理")
@RestController
@RequestMapping("/api/trade/order")
@RequiredArgsConstructor
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    @Operation(summary = "订单列表（分页）")
    @GetMapping("/list")
    public R<PageResult<TradeOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) Long buyerId) {
        return R.ok(PageResult.of(tradeOrderService.listOrders(page, size, keyword, status, farmerId, buyerId)));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public R<TradeOrder> detail(@PathVariable Long id) {
        return R.ok(tradeOrderService.getOrderDetail(id));
    }

    @Operation(summary = "创建交易订单")
    @PostMapping
    public R<TradeOrder> create(@RequestBody TradeOrder order) {
        return R.ok(tradeOrderService.createOrder(order));
    }

    @Operation(summary = "确认订单")
    @PutMapping("/{id}/confirm")
    public R<TradeOrder> confirm(@PathVariable Long id) {
        return R.ok(tradeOrderService.confirmOrder(id));
    }

    @Operation(summary = "标记发货")
    @PutMapping("/{id}/deliver")
    public R<TradeOrder> deliver(@PathVariable Long id) {
        return R.ok(tradeOrderService.deliverOrder(id));
    }

    @Operation(summary = "完成订单")
    @PutMapping("/{id}/complete")
    public R<TradeOrder> complete(@PathVariable Long id) {
        return R.ok(tradeOrderService.completeOrder(id));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public R<TradeOrder> cancel(@PathVariable Long id) {
        return R.ok(tradeOrderService.cancelOrder(id));
    }

    @Operation(summary = "导出订单CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            @RequestParam(required = false) Long buyerId,
            HttpServletResponse response) {
        tradeOrderService.exportOrders(keyword, status, farmerId, buyerId, response);
    }
}
