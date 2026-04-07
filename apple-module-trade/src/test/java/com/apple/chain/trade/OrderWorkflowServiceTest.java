package com.apple.chain.trade;

import com.apple.chain.common.exception.BizException;
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
import com.apple.chain.trade.service.impl.OrderWorkflowServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("M8 OrderWorkflowService 单元测试")
class OrderWorkflowServiceTest {

    @Mock private TradeOrderMapper tradeOrderMapper;
    @Mock private TradeContractMapper tradeContractMapper;
    @Mock private TradePaymentMapper tradePaymentMapper;
    @Mock private TradeInvoiceMapper tradeInvoiceMapper;
    @Mock private ContractGateway contractGateway;
    @Mock private PaymentGateway paymentGateway;
    @Mock private InvoiceGateway invoiceGateway;

    @InjectMocks
    private OrderWorkflowServiceImpl service;

    private TradeOrder draftOrder() {
        TradeOrder o = new TradeOrder();
        o.setId(1L);
        o.setOrderNo("ORD202604070001");
        o.setBuyerId(200L);
        o.setFarmerId(100L);
        o.setTotalAmount(new BigDecimal("1000.00"));
        o.setOrderStatus(OrderWorkflowServiceImpl.OS_DRAFT);
        return o;
    }

    @Test
    @DisplayName("createContract: DRAFT → CONTRACT_SIGNING")
    void createContract_happyPath() {
        TradeOrder order = draftOrder();
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        given(tradeContractMapper.findByOrderId(1L)).willReturn(null);
        given(contractGateway.createContract(1L, 100L, 200L)).willReturn("MOCK-CONTRACT-1-1");
        given(tradeContractMapper.insert(any(TradeContract.class))).willReturn(1);
        given(tradeOrderMapper.updateById(any(TradeOrder.class))).willReturn(1);

        TradeContract c = service.createContract(1L, 100L, 200L);

        assertThat(c.getContractNo()).isEqualTo("MOCK-CONTRACT-1-1");
        assertThat(c.getStatus()).isEqualTo(TradeContract.STATUS_PENDING);
        assertThat(order.getOrderStatus()).isEqualTo(OrderWorkflowServiceImpl.OS_CONTRACT_SIGNING);
    }

    @Test
    @DisplayName("createContract 非 DRAFT 状态拒绝")
    void createContract_rejectsNonDraft() {
        TradeOrder order = draftOrder();
        order.setOrderStatus(OrderWorkflowServiceImpl.OS_PAID);
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        assertThatThrownBy(() -> service.createContract(1L, 100L, 200L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单状态非法");
    }

    @Test
    @DisplayName("signContract 双方都签 → 状态推进 SIGNED")
    void signContract_bothParties() {
        TradeOrder order = draftOrder();
        order.setOrderStatus(OrderWorkflowServiceImpl.OS_CONTRACT_SIGNING);
        TradeContract contract = new TradeContract();
        contract.setOrderId(1L);
        contract.setContractNo("MOCK-CONTRACT-1-1");
        contract.setPartyA(100L);
        contract.setPartyB(200L);
        contract.setPartyASigned(0);
        contract.setPartyBSigned(0);
        contract.setStatus(TradeContract.STATUS_PENDING);
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        given(tradeContractMapper.findByOrderId(1L)).willReturn(contract);
        given(tradeContractMapper.updateById(any(TradeContract.class))).willReturn(1);
        given(tradeOrderMapper.updateById(any(TradeOrder.class))).willReturn(1);
        given(contractGateway.signContract(anyString(), anyLong())).willReturn("mock://x.pdf");

        // Party A signs
        TradeContract afterA = service.signContract(1L, 100L);
        assertThat(afterA.getPartyASigned()).isEqualTo(1);
        assertThat(afterA.getStatus()).isEqualTo(TradeContract.STATUS_PENDING); // still pending B

        // Party B signs
        TradeContract afterB = service.signContract(1L, 200L);
        assertThat(afterB.getPartyBSigned()).isEqualTo(1);
        assertThat(afterB.getStatus()).isEqualTo(TradeContract.STATUS_SIGNED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderWorkflowServiceImpl.OS_CONTRACT_SIGNED);
    }

    @Test
    @DisplayName("signContract 第三方签署 → 拒绝")
    void signContract_strangerForbidden() {
        TradeOrder order = draftOrder();
        order.setOrderStatus(OrderWorkflowServiceImpl.OS_CONTRACT_SIGNING);
        TradeContract contract = new TradeContract();
        contract.setOrderId(1L);
        contract.setContractNo("MOCK-CONTRACT-1-1");
        contract.setPartyA(100L);
        contract.setPartyB(200L);
        contract.setPartyASigned(0);
        contract.setPartyBSigned(0);
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        given(tradeContractMapper.findByOrderId(1L)).willReturn(contract);
        given(contractGateway.signContract(anyString(), anyLong())).willReturn("mock://x.pdf");

        assertThatThrownBy(() -> service.signContract(1L, 999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不在合同当事人");
    }

    @Test
    @DisplayName("createPayment: CONTRACT_SIGNED → PAYING")
    void createPayment_happyPath() {
        TradeOrder order = draftOrder();
        order.setOrderStatus(OrderWorkflowServiceImpl.OS_CONTRACT_SIGNED);
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        given(paymentGateway.createPayment(eq(1L), any(BigDecimal.class), eq("WECHAT")))
                .willReturn(new PaymentGateway.PaymentInit("MOCK-PAY-1", "mock://prepay"));
        given(tradePaymentMapper.insert(any(TradePayment.class))).willReturn(1);
        given(tradeOrderMapper.updateById(any(TradeOrder.class))).willReturn(1);

        TradePayment p = service.createPayment(1L, "WECHAT");
        assertThat(p.getPaymentNo()).isEqualTo("MOCK-PAY-1");
        assertThat(p.getStatus()).isEqualTo(TradePayment.STATUS_PAYING);
        assertThat(order.getOrderStatus()).isEqualTo(OrderWorkflowServiceImpl.OS_PAYING);
    }

    @Test
    @DisplayName("confirmPayment 触发自动开票 → 完整流程到 INVOICED")
    void confirmPayment_autoInvoice() {
        TradeOrder order = draftOrder();
        order.setOrderStatus(OrderWorkflowServiceImpl.OS_PAYING);
        TradePayment payment = new TradePayment();
        payment.setId(50L);
        payment.setOrderId(1L);
        payment.setPaymentNo("MOCK-PAY-1");
        payment.setAmount(new BigDecimal("1000.00"));
        payment.setStatus(TradePayment.STATUS_PAYING);

        given(tradePaymentMapper.findByPaymentNo("MOCK-PAY-1")).willReturn(payment);
        given(tradePaymentMapper.updateById(any(TradePayment.class))).willReturn(1);
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        given(tradeOrderMapper.updateById(any(TradeOrder.class))).willReturn(1);
        given(paymentGateway.confirmPayment("MOCK-PAY-1")).willReturn("MOCK-WX-TRADE-1");
        given(tradeInvoiceMapper.findByOrderId(1L)).willReturn(null);
        given(tradePaymentMapper.selectOne(any())).willReturn(payment);
        given(invoiceGateway.issue(eq(1L), any(BigDecimal.class), anyString(), anyString()))
                .willReturn(new InvoiceGateway.InvoiceResult(
                        "MOCK-INV-1", "MOCK-CODE-1", "mock://x.pdf", new BigDecimal("130.00")));
        given(tradeInvoiceMapper.insert(any(TradeInvoice.class))).willReturn(1);

        TradePayment result = service.confirmPayment("MOCK-PAY-1");

        assertThat(result.getStatus()).isEqualTo(TradePayment.STATUS_SUCCESS);
        assertThat(result.getThirdTradeNo()).isEqualTo("MOCK-WX-TRADE-1");
        assertThat(order.getOrderStatus()).isEqualTo(OrderWorkflowServiceImpl.OS_INVOICED);
        then(invoiceGateway).should().issue(eq(1L), any(BigDecimal.class), anyString(), anyString());
    }

    @Test
    @DisplayName("confirmPayment 已成功 → 幂等")
    void confirmPayment_idempotent() {
        TradePayment p = new TradePayment();
        p.setStatus(TradePayment.STATUS_SUCCESS);
        given(tradePaymentMapper.findByPaymentNo("MOCK")).willReturn(p);
        TradePayment result = service.confirmPayment("MOCK");
        assertThat(result).isSameAs(p);
        then(paymentGateway).should(never()).confirmPayment(anyString());
    }

    @Test
    @DisplayName("issueInvoice 未支付订单拒绝")
    void issueInvoice_rejectsUnpaid() {
        TradeOrder order = draftOrder();
        order.setOrderStatus(OrderWorkflowServiceImpl.OS_PAYING);
        given(tradeOrderMapper.selectById(1L)).willReturn(order);
        assertThatThrownBy(() -> service.issueInvoice(1L, "x", "y"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单状态非法");
    }
}
