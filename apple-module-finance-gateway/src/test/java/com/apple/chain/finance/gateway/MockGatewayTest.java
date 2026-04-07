package com.apple.chain.finance.gateway;

import com.apple.chain.finance.gateway.impl.MockBaiwangInvoiceGateway;
import com.apple.chain.finance.gateway.impl.MockESignContractGateway;
import com.apple.chain.finance.gateway.impl.MockWechatPaymentGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("M8 Mock Gateway 单元测试")
class MockGatewayTest {

    @Test
    @DisplayName("MockESignContractGateway 生成唯一 contractNo + PDF URL")
    void contractGateway() {
        MockESignContractGateway g = new MockESignContractGateway();
        String c1 = g.createContract(1L, 100L, 200L);
        String c2 = g.createContract(1L, 100L, 200L);
        assertThat(c1).startsWith("MOCK-CONTRACT-1-");
        assertThat(c1).isNotEqualTo(c2);

        String pdf = g.signContract(c1, 100L);
        assertThat(pdf).startsWith("mock://contracts/");
        assertThat(pdf).contains(c1);
    }

    @Test
    @DisplayName("MockWechatPaymentGateway init + confirm")
    void paymentGateway() {
        MockWechatPaymentGateway g = new MockWechatPaymentGateway();
        PaymentGateway.PaymentInit init = g.createPayment(10L, BigDecimal.valueOf(100), "WECHAT");
        assertThat(init.paymentNo()).startsWith("MOCK-PAY-10-");
        assertThat(init.prepayParams()).contains("amt=100");

        String tradeNo = g.confirmPayment(init.paymentNo());
        assertThat(tradeNo).startsWith("MOCK-WX-TRADE-");
        assertThat(tradeNo).contains(init.paymentNo());
    }

    @Test
    @DisplayName("MockBaiwangInvoiceGateway 13% VAT split")
    void invoiceGateway() {
        MockBaiwangInvoiceGateway g = new MockBaiwangInvoiceGateway();
        InvoiceGateway.InvoiceResult r = g.issue(10L, new BigDecimal("100.00"), "test", "tax-no");
        assertThat(r.invoiceNo()).startsWith("MOCK-INV-");
        assertThat(r.invoiceCode()).contains("10");
        assertThat(r.pdfUrl()).startsWith("mock://invoices/");
        assertThat(r.taxAmount()).isEqualByComparingTo("13.00");
    }
}
