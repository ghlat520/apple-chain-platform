package com.apple.chain.finance.gateway.impl;

import com.apple.chain.finance.gateway.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock WeChat payment gateway. {@link #createPayment} returns a fake prepayId;
 * {@link #confirmPayment} synthesizes a tradeNo so the workflow can complete
 * without a real third-party callback.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "finance.gateway.payment.real",
        havingValue = "false", matchIfMissing = true)
public class MockWechatPaymentGateway implements PaymentGateway {

    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public PaymentInit createPayment(Long orderId, BigDecimal amount, String channel) {
        long seq = counter.getAndIncrement();
        String paymentNo = String.format("MOCK-PAY-%d-%06d", orderId == null ? 0 : orderId, seq);
        String prepay = "mock://prepay?pn=" + paymentNo + "&amt=" + amount + "&ch=" + channel;
        log.debug("Mock payment init: {}", paymentNo);
        return new PaymentInit(paymentNo, prepay);
    }

    @Override
    public String confirmPayment(String paymentNo) {
        return "MOCK-WX-TRADE-" + paymentNo;
    }
}
