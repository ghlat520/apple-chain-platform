package com.apple.chain.finance.gateway;

import java.math.BigDecimal;

/**
 * M8 invoice gateway. Wraps 百望云 / 金税三期.
 */
public interface InvoiceGateway {

    record InvoiceResult(String invoiceNo, String invoiceCode, String pdfUrl, BigDecimal taxAmount) {}

    /**
     * Issue a VAT invoice synchronously.
     * Real implementation may take up to 30s; mock returns immediately.
     */
    InvoiceResult issue(Long orderId, BigDecimal amount, String taxPayer, String taxNo);
}
