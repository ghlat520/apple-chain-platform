package com.apple.chain.finance.gateway.impl;

import com.apple.chain.finance.gateway.InvoiceGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock 百望云 invoice gateway. Generates deterministic invoice numbers and
 * computes a 13% VAT split off the gross amount.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "finance.gateway.invoice.real",
        havingValue = "false", matchIfMissing = true)
public class MockBaiwangInvoiceGateway implements InvoiceGateway {

    /** China VAT standard rate for fresh agricultural products (mock). */
    private static final BigDecimal VAT_RATE = new BigDecimal("0.13");

    private final AtomicLong counter = new AtomicLong(1);

    @Override
    public InvoiceResult issue(Long orderId, BigDecimal amount, String taxPayer, String taxNo) {
        long seq = counter.getAndIncrement();
        String invoiceNo = String.format("MOCK-INV-%06d", seq);
        String invoiceCode = "MOCK-CODE-" + (orderId == null ? 0 : orderId);
        BigDecimal tax = amount == null ? BigDecimal.ZERO
                : amount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        String pdfUrl = "mock://invoices/" + invoiceNo + ".pdf";
        log.debug("Mock invoice issued: {} for order {} amount {}", invoiceNo, orderId, amount);
        return new InvoiceResult(invoiceNo, invoiceCode, pdfUrl, tax);
    }
}
