package com.apple.chain.trade.wxpay.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.wxpay.dto.WxPayCreateRequest;
import com.apple.chain.trade.wxpay.dto.WxPayResponse;
import com.apple.chain.trade.wxpay.dto.WxReconcileResponse;
import com.apple.chain.trade.wxpay.dto.WxRefundCreateRequest;
import com.apple.chain.trade.wxpay.dto.WxRefundResponse;
import com.apple.chain.trade.wxpay.service.MockWxPayService;
import com.apple.chain.trade.wxpay.service.MockWxReconcileService;
import com.apple.chain.trade.wxpay.service.MockWxRefundService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 微信支付 Mock Controller —— 演示原型，不对接真实微信支付。
 *
 * <p>提供三组端点：支付 / 退款 / 对账。所有金额以 <b>分</b> 为单位。
 */
@Tag(name = "微信支付 Mock（演示原型）")
@RestController
@RequiredArgsConstructor
public class WxPayController {

    private final MockWxPayService mockWxPayService;
    private final MockWxRefundService mockWxRefundService;
    private final MockWxReconcileService mockWxReconcileService;

    // ---------------- payments ----------------

    @Operation(summary = "发起 Mock 微信支付")
    @PostMapping("/api/wxpay/payments")
    public R<WxPayResponse> createPayment(@Valid @RequestBody WxPayCreateRequest request) {
        return R.ok(WxPayResponse.from(mockWxPayService.createPayment(request)));
    }

    @Operation(summary = "查询支付记录（Mock，自动按概率推进 PENDING → SUCCESS）")
    @GetMapping("/api/wxpay/payments/{id}")
    public R<WxPayResponse> queryPayment(@PathVariable Long id) {
        return R.ok(WxPayResponse.from(mockWxPayService.queryPayment(id)));
    }

    @Operation(summary = "支付记录分页列表")
    @GetMapping("/api/wxpay/payments")
    public R<PageResult<WxPayResponse>> listPayments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        IPage<com.apple.chain.trade.wxpay.entity.WxPayment> p =
                mockWxPayService.listPayments(page, size, keyword, status);
        List<WxPayResponse> rows = p.getRecords().stream().map(WxPayResponse::from).toList();
        return R.ok(PageResult.of(rows, p.getTotal(), p.getCurrent(), p.getSize()));
    }

    // ---------------- refunds ----------------

    @Operation(summary = "发起 Mock 微信退款")
    @PostMapping("/api/wxpay/refunds")
    public R<WxRefundResponse> createRefund(@Valid @RequestBody WxRefundCreateRequest request) {
        return R.ok(WxRefundResponse.from(mockWxRefundService.createRefund(request)));
    }

    @Operation(summary = "查询退款记录")
    @GetMapping("/api/wxpay/refunds/{id}")
    public R<WxRefundResponse> getRefund(@PathVariable Long id) {
        return R.ok(WxRefundResponse.from(mockWxRefundService.getRefund(id)));
    }

    @Operation(summary = "按支付记录查询其退款明细")
    @GetMapping("/api/wxpay/refunds")
    public R<List<WxRefundResponse>> listRefundsByPayment(@RequestParam Long paymentId) {
        return R.ok(mockWxRefundService.listByPaymentId(paymentId).stream()
                .map(WxRefundResponse::from)
                .toList());
    }

    // ---------------- reconcile ----------------

    @Operation(summary = "按日查询 Mock 对账单")
    @GetMapping("/api/wxpay/reconcile")
    public R<WxReconcileResponse> reconcile(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(mockWxReconcileService.reconcileByDate(date));
    }
}
