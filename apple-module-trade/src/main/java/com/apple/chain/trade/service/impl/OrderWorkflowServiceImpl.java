package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.gateway.ContractGateway;
import com.apple.chain.finance.gateway.InvoiceGateway;
import com.apple.chain.finance.gateway.PaymentGateway;
import com.apple.chain.trade.entity.TradeContract;
import com.apple.chain.trade.entity.TradeInvoice;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.entity.TradePayment;
import com.apple.chain.trade.mapper.TradeContractMapper;
import com.apple.chain.trade.mapper.TradeInvoiceMapper;
import com.apple.chain.trade.mapper.TradeOrderMapper;
import com.apple.chain.trade.mapper.TradePaymentMapper;
import com.apple.chain.trade.service.OrderWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * M8 workflow orchestration. Each step:
 *   1. Validates current order status against the allowed transition.
 *   2. Calls the gateway adapter (mock or real, decided by Spring context).
 *   3. Persists the resulting contract/payment/invoice row.
 *   4. Bumps {@code td_trade_order.order_status} and {@code payment_status}.
 *
 * <p>Automatic invoice issuance: {@link #confirmPayment} calls
 * {@link #issueInvoice} synchronously. The plan called for an MQ-based
 * "payment-success → invoice" decoupling but the project has no MQ; sync is
 * acceptable since invoice issuance is fast (mock = ms; real = ~30s SLA).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderWorkflowServiceImpl implements OrderWorkflowService {

    // Order status machine
    public static final String OS_DRAFT             = "DRAFT";
    public static final String OS_CONTRACT_SIGNING  = "CONTRACT_SIGNING";
    public static final String OS_CONTRACT_SIGNED   = "CONTRACT_SIGNED";
    public static final String OS_PAYING            = "PAYING";
    public static final String OS_PAID              = "PAID";
    public static final String OS_INVOICING         = "INVOICING";
    public static final String OS_INVOICED          = "INVOICED";

    // Payment status machine
    public static final String PS_PENDING = "PENDING";
    public static final String PS_PAID    = "PAID";

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeContractMapper tradeContractMapper;
    private final TradePaymentMapper tradePaymentMapper;
    private final TradeInvoiceMapper tradeInvoiceMapper;
    private final ContractGateway contractGateway;
    private final PaymentGateway paymentGateway;
    private final InvoiceGateway invoiceGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeContract createContract(Long orderId, Long partyA, Long partyB) {
        TradeOrder order = requireOrder(orderId);
        requireStatus(order, OS_DRAFT);

        // Idempotent: if a contract already exists for this order, return it
        TradeContract existing = tradeContractMapper.findByOrderId(orderId);
        if (existing != null) {
            return existing;
        }

        String contractNo = contractGateway.createContract(orderId, partyA, partyB);
        TradeContract c = new TradeContract();
        c.setOrderId(orderId);
        c.setContractNo(contractNo);
        c.setPartyA(partyA);
        c.setPartyB(partyB);
        c.setPartyASigned(0);
        c.setPartyBSigned(0);
        c.setStatus(TradeContract.STATUS_PENDING);
        try {
            tradeContractMapper.insert(c);
        } catch (DuplicateKeyException e) {
            return tradeContractMapper.findByOrderId(orderId);
        }

        order.setOrderStatus(OS_CONTRACT_SIGNING);
        tradeOrderMapper.updateById(order);
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeContract signContract(Long orderId, Long signerUid) {
        TradeOrder order = requireOrder(orderId);
        if (!OS_CONTRACT_SIGNING.equals(order.getOrderStatus())
                && !OS_CONTRACT_SIGNED.equals(order.getOrderStatus())) {
            throw illegalState(order.getOrderStatus(), "CONTRACT_SIGNING");
        }
        TradeContract c = tradeContractMapper.findByOrderId(orderId);
        if (c == null) {
            throw new BizException(ResultCode.NOT_FOUND, "合同不存在");
        }

        contractGateway.signContract(c.getContractNo(), signerUid);

        // Mark which party signed
        boolean isA = signerUid != null && signerUid.equals(c.getPartyA());
        boolean isB = signerUid != null && signerUid.equals(c.getPartyB());
        if (!isA && !isB) {
            throw new BizException(ResultCode.FORBIDDEN, "签署者不在合同当事人列表");
        }
        if (isA) {
            c.setPartyASigned(1);
            c.setPartyASignTime(LocalDateTime.now());
        }
        if (isB) {
            c.setPartyBSigned(1);
            c.setPartyBSignTime(LocalDateTime.now());
        }
        if (c.getPartyASigned() == 1 && c.getPartyBSigned() == 1) {
            c.setStatus(TradeContract.STATUS_SIGNED);
            order.setOrderStatus(OS_CONTRACT_SIGNED);
            tradeOrderMapper.updateById(order);
        }
        tradeContractMapper.updateById(c);
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradePayment createPayment(Long orderId, String channel) {
        TradeOrder order = requireOrder(orderId);
        requireStatus(order, OS_CONTRACT_SIGNED);

        PaymentGateway.PaymentInit init = paymentGateway.createPayment(
                orderId, order.getTotalAmount(), channel);

        TradePayment p = new TradePayment();
        p.setOrderId(orderId);
        p.setPaymentNo(init.paymentNo());
        p.setChannel(channel);
        p.setAmount(order.getTotalAmount());
        p.setStatus(TradePayment.STATUS_PAYING);
        tradePaymentMapper.insert(p);

        order.setOrderStatus(OS_PAYING);
        tradeOrderMapper.updateById(order);
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradePayment confirmPayment(String paymentNo) {
        TradePayment p = tradePaymentMapper.findByPaymentNo(paymentNo);
        if (p == null) {
            throw new BizException(ResultCode.NOT_FOUND, "支付不存在: " + paymentNo);
        }
        if (p.getStatus() == TradePayment.STATUS_SUCCESS) {
            return p; // idempotent
        }

        String tradeNo = paymentGateway.confirmPayment(paymentNo);
        p.setThirdTradeNo(tradeNo);
        p.setStatus(TradePayment.STATUS_SUCCESS);
        p.setPayTime(LocalDateTime.now());
        p.setCallbackTime(LocalDateTime.now());
        tradePaymentMapper.updateById(p);

        TradeOrder order = requireOrder(p.getOrderId());
        order.setOrderStatus(OS_PAID);
        order.setPaymentStatus(PS_PAID);
        tradeOrderMapper.updateById(order);

        // Auto-issue invoice with placeholder tax info; production wires real
        // taxpayer profile from buyer settings
        try {
            issueInvoice(p.getOrderId(), "默认抬头", "MOCK-TAX-NO");
        } catch (Exception e) {
            log.warn("Auto invoice failed for order {}: {}", p.getOrderId(), e.toString());
            // Order stays at PAID; admin can retry invoice manually
        }
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeInvoice issueInvoice(Long orderId, String taxPayer, String taxNo) {
        TradeOrder order = requireOrder(orderId);
        if (!OS_PAID.equals(order.getOrderStatus()) && !OS_INVOICED.equals(order.getOrderStatus())
                && !OS_INVOICING.equals(order.getOrderStatus())) {
            throw illegalState(order.getOrderStatus(), "PAID");
        }

        TradeInvoice existing = tradeInvoiceMapper.findByOrderId(orderId);
        if (existing != null && existing.getStatus() == TradeInvoice.STATUS_SUCCESS) {
            return existing;
        }

        // Find the latest paid payment for the order
        TradePayment payment = tradePaymentMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TradePayment>()
                        .eq(TradePayment::getOrderId, orderId)
                        .eq(TradePayment::getStatus, TradePayment.STATUS_SUCCESS)
                        .orderByDesc(TradePayment::getId)
                        .last("LIMIT 1"));
        if (payment == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "订单尚未支付完成");
        }

        order.setOrderStatus(OS_INVOICING);
        tradeOrderMapper.updateById(order);

        InvoiceGateway.InvoiceResult result = invoiceGateway.issue(
                orderId, order.getTotalAmount(), taxPayer, taxNo);

        TradeInvoice inv = new TradeInvoice();
        inv.setOrderId(orderId);
        inv.setPaymentId(payment.getId());
        inv.setInvoiceNo(result.invoiceNo());
        inv.setInvoiceCode(result.invoiceCode());
        inv.setTaxPayer(taxPayer);
        inv.setTaxNo(taxNo);
        inv.setAmount(order.getTotalAmount());
        inv.setTaxAmount(result.taxAmount());
        inv.setPdfUrl(result.pdfUrl());
        inv.setStatus(TradeInvoice.STATUS_SUCCESS);
        inv.setIssueTime(LocalDateTime.now());
        tradeInvoiceMapper.insert(inv);

        order.setOrderStatus(OS_INVOICED);
        tradeOrderMapper.updateById(order);
        return inv;
    }

    // ==== helpers ====

    private TradeOrder requireOrder(Long orderId) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在: " + orderId);
        }
        return order;
    }

    private void requireStatus(TradeOrder order, String expected) {
        if (!expected.equals(order.getOrderStatus())) {
            throw illegalState(order.getOrderStatus(), expected);
        }
    }

    private static BizException illegalState(String actual, String expected) {
        return new BizException(ResultCode.PARAM_ERROR,
                "订单状态非法: 当前=" + actual + " 期望=" + expected);
    }
}
