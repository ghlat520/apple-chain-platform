package com.apple.chain.finance;

import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.gateway.ContractGateway;
import com.apple.chain.finance.gateway.InvoiceGateway;
import com.apple.chain.finance.gateway.PaymentGateway;
import com.apple.chain.finance.service.LoanService;
import com.apple.chain.finance.service.impl.LoanWorkflowServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * M7 LoanWorkflowService unit tests.
 *
 * Tests the workflow wiring: submit loan -> gateway contract -> invoice -> payment.
 * All external gateways are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanWorkflowService - 贷款工作流服务")
class LoanWorkflowServiceTest {

    @Mock
    private LoanService loanService;

    @Mock
    private ContractGateway contractGateway;

    @Mock
    private InvoiceGateway invoiceGateway;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private LoanWorkflowServiceImpl loanWorkflowService;

    // ── Helper ──────────────────────────────────────────────────────

    private Loan makeLoan(Long id) {
        Loan loan = new Loan();
        loan.setId(id);
        loan.setLoanCode("LN202604010001");
        loan.setBorrowerName("Test Farm Co.");
        loan.setBorrowerType("ENTERPRISE");
        loan.setBorrowerId(10L);
        loan.setAmount(new BigDecimal("500000"));
        loan.setInterestRate(new BigDecimal("4.5"));
        loan.setTermMonths(12);
        loan.setStatus("APPROVED");
        return loan;
    }

    // ── processLoanWithContract ─────────────────────────────────────

    @Nested
    @DisplayName("processLoanWithContract - 合同+发票+支付全流程")
    class ProcessLoanWithContract {

        @Test
        @DisplayName("完整流程: 创建合同 -> 签署 -> 开票 -> 创建支付 -> 确认支付")
        void full_workflow_calls_all_gateways_in_order() {
            Loan loan = makeLoan(1L);
            when(loanService.getLoanDetail(1L)).thenReturn(loan);

            // Gateway responses
            String contractNo = "CONTRACT-2026-001";
            String pdfUrl = "https://minio/mock-contracts/CONTRACT-2026-001.pdf";
            when(contractGateway.createContract(eq(1L), eq(10L), eq(1L)))
                    .thenReturn(contractNo);
            when(contractGateway.signContract(eq(contractNo), eq(10L)))
                    .thenReturn(pdfUrl);

            InvoiceGateway.InvoiceResult invoiceResult =
                    new InvoiceGateway.InvoiceResult(
                            "INV-001", "INV-CODE-001", "https://invoice.pdf",
                            new BigDecimal("25000"));
            when(invoiceGateway.issue(eq(1L), eq(new BigDecimal("500000")),
                    eq("Test Farm Co."), eq("UNKNOWN")))
                    .thenReturn(invoiceResult);

            PaymentGateway.PaymentInit paymentInit =
                    new PaymentGateway.PaymentInit("PAY-001", "prepay-params");
            when(paymentGateway.createPayment(eq(1L), eq(new BigDecimal("500000")),
                    eq("BANK_TRANSFER")))
                    .thenReturn(paymentInit);
            when(paymentGateway.confirmPayment(eq("PAY-001")))
                    .thenReturn("TRADE-NO-001");

            // Execute
            loanWorkflowService.processLoanWithContract(1L);

            // Verify contract gateway calls
            verify(contractGateway).createContract(1L, 10L, 1L);
            verify(contractGateway).signContract(contractNo, 10L);

            // Verify invoice gateway calls
            verify(invoiceGateway).issue(1L, new BigDecimal("500000"),
                    "Test Farm Co.", "UNKNOWN");

            // Verify payment gateway calls
            verify(paymentGateway).createPayment(1L, new BigDecimal("500000"),
                    "BANK_TRANSFER");
            verify(paymentGateway).confirmPayment("PAY-001");
        }

        @Test
        @DisplayName("合同网关使用正确的借款人ID")
        void contract_gateway_uses_borrower_id() {
            Loan loan = makeLoan(1L);
            loan.setBorrowerId(42L);
            when(loanService.getLoanDetail(1L)).thenReturn(loan);
            when(contractGateway.createContract(anyLong(), anyLong(), anyLong()))
                    .thenReturn("CONTRACT-NO");
            when(contractGateway.signContract(anyString(), anyLong()))
                    .thenReturn("http://pdf.url");
            when(invoiceGateway.issue(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new InvoiceGateway.InvoiceResult(
                            "INV", "CODE", "url", BigDecimal.ZERO));
            when(paymentGateway.createPayment(anyLong(), any(), anyString()))
                    .thenReturn(new PaymentGateway.PaymentInit("PAY", "params"));
            when(paymentGateway.confirmPayment(anyString()))
                    .thenReturn("TRADE");

            loanWorkflowService.processLoanWithContract(1L);

            verify(contractGateway).createContract(1L, 42L, 1L);
            verify(contractGateway).signContract("CONTRACT-NO", 42L);
        }

        @Test
        @DisplayName("发票网关使用正确的金额和纳税人名称")
        void invoice_gateway_uses_correct_params() {
            Loan loan = makeLoan(1L);
            loan.setAmount(new BigDecimal("1234567"));
            loan.setBorrowerName("ABC Orchard");
            when(loanService.getLoanDetail(1L)).thenReturn(loan);
            when(contractGateway.createContract(anyLong(), anyLong(), anyLong()))
                    .thenReturn("CONTRACT-NO");
            when(contractGateway.signContract(anyString(), anyLong()))
                    .thenReturn("http://pdf.url");
            when(invoiceGateway.issue(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new InvoiceGateway.InvoiceResult(
                            "INV", "CODE", "url", BigDecimal.ZERO));
            when(paymentGateway.createPayment(anyLong(), any(), anyString()))
                    .thenReturn(new PaymentGateway.PaymentInit("PAY", "params"));
            when(paymentGateway.confirmPayment(anyString()))
                    .thenReturn("TRADE");

            loanWorkflowService.processLoanWithContract(1L);

            verify(invoiceGateway).issue(
                    eq(1L),
                    eq(new BigDecimal("1234567")),
                    eq("ABC Orchard"),
                    eq("UNKNOWN"));
        }

        @Test
        @DisplayName("支付网关使用 BANK_TRANSFER 渠道")
        void payment_gateway_uses_bank_transfer() {
            Loan loan = makeLoan(1L);
            when(loanService.getLoanDetail(1L)).thenReturn(loan);
            when(contractGateway.createContract(anyLong(), anyLong(), anyLong()))
                    .thenReturn("CONTRACT-NO");
            when(contractGateway.signContract(anyString(), anyLong()))
                    .thenReturn("http://pdf.url");
            when(invoiceGateway.issue(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new InvoiceGateway.InvoiceResult(
                            "INV", "CODE", "url", BigDecimal.ZERO));
            when(paymentGateway.createPayment(anyLong(), any(), anyString()))
                    .thenReturn(new PaymentGateway.PaymentInit("PAY-NO", "params"));
            when(paymentGateway.confirmPayment(anyString()))
                    .thenReturn("TRADE-NO");

            loanWorkflowService.processLoanWithContract(1L);

            verify(paymentGateway).createPayment(1L, new BigDecimal("500000"), "BANK_TRANSFER");
            verify(paymentGateway).confirmPayment("PAY-NO");
        }

        @Test
        @DisplayName("流程步骤严格有序: 合同 -> 发票 -> 支付")
        void gateway_calls_in_correct_order() {
            Loan loan = makeLoan(1L);
            when(loanService.getLoanDetail(1L)).thenReturn(loan);
            when(contractGateway.createContract(anyLong(), anyLong(), anyLong()))
                    .thenReturn("CONTRACT-NO");
            when(contractGateway.signContract(anyString(), anyLong()))
                    .thenReturn("http://pdf.url");
            when(invoiceGateway.issue(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new InvoiceGateway.InvoiceResult(
                            "INV", "CODE", "url", BigDecimal.ZERO));
            when(paymentGateway.createPayment(anyLong(), any(), anyString()))
                    .thenReturn(new PaymentGateway.PaymentInit("PAY-NO", "params"));
            when(paymentGateway.confirmPayment(anyString()))
                    .thenReturn("TRADE-NO");

            loanWorkflowService.processLoanWithContract(1L);

            var inOrder = inOrder(contractGateway, invoiceGateway, paymentGateway);
            inOrder.verify(contractGateway).createContract(anyLong(), anyLong(), anyLong());
            inOrder.verify(contractGateway).signContract(anyString(), anyLong());
            inOrder.verify(invoiceGateway).issue(anyLong(), any(), anyString(), anyString());
            inOrder.verify(paymentGateway).createPayment(anyLong(), any(), anyString());
            inOrder.verify(paymentGateway).confirmPayment(anyString());
        }

        @Test
        @DisplayName("合同网关 createContract 只调用一次")
        void contract_create_called_once() {
            Loan loan = makeLoan(1L);
            when(loanService.getLoanDetail(1L)).thenReturn(loan);
            when(contractGateway.createContract(anyLong(), anyLong(), anyLong()))
                    .thenReturn("CONTRACT-NO");
            when(contractGateway.signContract(anyString(), anyLong()))
                    .thenReturn("http://pdf.url");
            when(invoiceGateway.issue(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new InvoiceGateway.InvoiceResult(
                            "INV", "CODE", "url", BigDecimal.ZERO));
            when(paymentGateway.createPayment(anyLong(), any(), anyString()))
                    .thenReturn(new PaymentGateway.PaymentInit("PAY-NO", "params"));
            when(paymentGateway.confirmPayment(anyString()))
                    .thenReturn("TRADE-NO");

            loanWorkflowService.processLoanWithContract(1L);

            verify(contractGateway, times(1)).createContract(anyLong(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("发票网关 issue 只调用一次")
        void invoice_issue_called_once() {
            Loan loan = makeLoan(1L);
            when(loanService.getLoanDetail(1L)).thenReturn(loan);
            when(contractGateway.createContract(anyLong(), anyLong(), anyLong()))
                    .thenReturn("CONTRACT-NO");
            when(contractGateway.signContract(anyString(), anyLong()))
                    .thenReturn("http://pdf.url");
            when(invoiceGateway.issue(anyLong(), any(), anyString(), anyString()))
                    .thenReturn(new InvoiceGateway.InvoiceResult(
                            "INV", "CODE", "url", BigDecimal.ZERO));
            when(paymentGateway.createPayment(anyLong(), any(), anyString()))
                    .thenReturn(new PaymentGateway.PaymentInit("PAY-NO", "params"));
            when(paymentGateway.confirmPayment(anyString()))
                    .thenReturn("TRADE-NO");

            loanWorkflowService.processLoanWithContract(1L);

            verify(invoiceGateway, times(1)).issue(anyLong(), any(), anyString(), anyString());
        }
    }
}
