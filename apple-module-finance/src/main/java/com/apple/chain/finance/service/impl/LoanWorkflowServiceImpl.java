package com.apple.chain.finance.service.impl;

import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.gateway.ContractGateway;
import com.apple.chain.finance.gateway.InvoiceGateway;
import com.apple.chain.finance.gateway.PaymentGateway;
import com.apple.chain.finance.service.LoanService;
import com.apple.chain.finance.service.LoanWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanWorkflowServiceImpl implements LoanWorkflowService {

    private final LoanService loanService;
    private final ContractGateway contractGateway;
    private final InvoiceGateway invoiceGateway;
    private final PaymentGateway paymentGateway;

    @Override
    public void processLoanWithContract(Long loanId) {
        Loan loan = loanService.getLoanDetail(loanId);

        // Step 1: create and sign contract
        String contractNo = contractGateway.createContract(loanId, loan.getBorrowerId(), 1L);
        String pdfUrl = contractGateway.signContract(contractNo, loan.getBorrowerId());
        log.info("Loan {} contract signed: contractNo={}, pdfUrl={}", loanId, contractNo, pdfUrl);

        // Step 2: issue invoice
        InvoiceGateway.InvoiceResult invoiceResult = invoiceGateway.issue(
                loanId,
                loan.getAmount(),
                loan.getBorrowerName(),
                "UNKNOWN"
        );
        log.info("Loan {} invoice issued: invoiceNo={}, taxAmount={}", loanId, invoiceResult.invoiceNo(), invoiceResult.taxAmount());

        // Step 3: create payment session
        PaymentGateway.PaymentInit paymentInit = paymentGateway.createPayment(loanId, loan.getAmount(), "BANK_TRANSFER");
        String tradeNo = paymentGateway.confirmPayment(paymentInit.paymentNo());
        log.info("Loan {} payment confirmed: paymentNo={}, tradeNo={}", loanId, paymentInit.paymentNo(), tradeNo);
    }
}
