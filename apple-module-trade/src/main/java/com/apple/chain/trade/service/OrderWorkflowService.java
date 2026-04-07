package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.TradeContract;
import com.apple.chain.trade.entity.TradeInvoice;
import com.apple.chain.trade.entity.TradePayment;

/**
 * M8 order workflow orchestrator.
 *
 * State machine:
 * <pre>
 *   DRAFT → CONTRACT_SIGNING → CONTRACT_SIGNED
 *         → PAYING → PAID
 *         → INVOICING → INVOICED
 *         → SHIPPING → SHIPPED → COMPLETED
 * </pre>
 *
 * The {@link TradeOrderService#createOrder} call still produces the order in DRAFT.
 * From there, M8 takes over via the methods below.
 */
public interface OrderWorkflowService {

    /** Step 1: create the contract draft via {@code ContractGateway}. */
    TradeContract createContract(Long orderId, Long partyA, Long partyB);

    /** Step 2: sign the contract on behalf of one party. Both parties must call. */
    TradeContract signContract(Long orderId, Long signerUid);

    /** Step 3: create a payment session. */
    TradePayment createPayment(Long orderId, String channel);

    /**
     * Step 4: payment-success callback (mock-mode: called manually by simulator).
     * Triggers automatic invoice issuance via {@link InvoiceGateway}.
     */
    TradePayment confirmPayment(String paymentNo);

    /** Step 5 (auto on payment success): issue VAT invoice. */
    TradeInvoice issueInvoice(Long orderId, String taxPayer, String taxNo);
}
