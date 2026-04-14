package com.apple.chain.trade.wxpay.service;

import com.apple.chain.trade.wxpay.dto.WxPayResponse;
import com.apple.chain.trade.wxpay.dto.WxReconcileResponse;
import com.apple.chain.trade.wxpay.dto.WxRefundResponse;
import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.apple.chain.trade.wxpay.entity.WxRefund;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 微信对账 Mock 服务 —— 按日聚合支付和退款明细，供演示使用。
 */
@Service
@RequiredArgsConstructor
public class MockWxReconcileService {

    private final MockWxPayService mockWxPayService;
    private final MockWxRefundService mockWxRefundService;

    public WxReconcileResponse reconcileByDate(LocalDate date) {
        List<WxPayment> payments = mockWxPayService.listSuccessByDate(date);
        List<WxRefund> refunds = mockWxRefundService.listSuccessByDate(date);

        long paymentTotal = payments.stream()
                .mapToLong(p -> p.getAmountCents() == null ? 0L : p.getAmountCents())
                .sum();
        long refundTotal = refunds.stream()
                .mapToLong(r -> r.getRefundAmountCents() == null ? 0L : r.getRefundAmountCents())
                .sum();

        return WxReconcileResponse.builder()
                .billDate(date)
                .paymentCount(payments.size())
                .paymentTotalCents(paymentTotal)
                .refundCount(refunds.size())
                .refundTotalCents(refundTotal)
                .netAmountCents(paymentTotal - refundTotal)
                .payments(payments.stream().map(WxPayResponse::from).toList())
                .refunds(refunds.stream().map(WxRefundResponse::from).toList())
                .build();
    }
}
