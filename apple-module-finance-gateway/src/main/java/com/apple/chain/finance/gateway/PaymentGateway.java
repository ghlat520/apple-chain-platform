package com.apple.chain.finance.gateway;

import java.math.BigDecimal;

/**
 * M8 payment gateway. Wraps WeChat Pay / UnionPay / bank transfer.
 */
public interface PaymentGateway {

    /** Result of {@link #createPayment}. */
    record PaymentInit(String paymentNo, String prepayParams) {}

    /**
     * Create a prepay session.
     *
     * @return payment number + opaque prepay params (QR code, redirect URL, etc.)
     */
    PaymentInit createPayment(Long orderId, BigDecimal amount, String channel);

    /**
     * Confirm a payment by its payment number. Used by callback handlers AND
     * by the mock simulator endpoint.
     *
     * @return third-party trade number (mock generates a synthetic one)
     */
    String confirmPayment(String paymentNo);
}
