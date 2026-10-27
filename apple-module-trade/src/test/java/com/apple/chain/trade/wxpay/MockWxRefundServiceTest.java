package com.apple.chain.trade.wxpay;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trade.wxpay.dto.WxRefundCreateRequest;
import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.apple.chain.trade.wxpay.entity.WxRefund;
import com.apple.chain.trade.wxpay.enums.WxPaymentStatus;
import com.apple.chain.trade.wxpay.enums.WxRefundStatus;
import com.apple.chain.trade.wxpay.service.MockWxPayService;
import com.apple.chain.trade.wxpay.service.MockWxRefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 单元测试 —— MockWxRefundService 业务校验（金额/状态/终态同步）。
 */
@DisplayName("MockWxRefundService 单元测试")
class MockWxRefundServiceTest {

    private WxPayment successPayment(long id, long amountCents) {
        WxPayment p = new WxPayment();
        p.setId(id);
        p.setAmountCents(amountCents);
        p.setStatus(WxPaymentStatus.SUCCESS);
        return p;
    }

    @Test
    @DisplayName("createRefund 成功 —— 退款立即置 SUCCESS，同时原支付置 REFUNDED")
    void createRefund_succeedsAndSyncsPayment() {
        WxPayment payment = successPayment(9046L, 31200L);
        AtomicReference<WxPayment> updatedPayment = new AtomicReference<>();

        MockWxPayService payService = new MockWxPayService() {
            @Override
            public WxPayment getPayment(Long id) {
                return payment;
            }
            @Override
            public boolean updateById(WxPayment entity) {
                updatedPayment.set(entity);
                return true;
            }
        };

        MockWxRefundService svc = new MockWxRefundService(payService) {
            @Override
            public boolean save(WxRefund entity) {
                entity.setId(9999L);
                return true;
            }
        };

        WxRefundCreateRequest req = new WxRefundCreateRequest();
        req.setPaymentId(9046L);
        req.setRefundAmountCents(31200L);
        req.setReason("果品到货质量不符");

        WxRefund refund = svc.createRefund(req);

        assertThat(refund.getStatus()).isEqualTo(WxRefundStatus.SUCCESS);
        assertThat(refund.getOutRefundNo()).startsWith("WXRFD");
        assertThat(refund.getRefundId()).startsWith("50000");
        assertThat(refund.getRefundTime()).isNotNull();
        assertThat(updatedPayment.get()).isNotNull();
        assertThat(updatedPayment.get().getStatus()).isEqualTo(WxPaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("createRefund 拒绝对非 SUCCESS 支付退款")
    void createRefund_rejectsNonSuccessPayment() {
        WxPayment pending = successPayment(1L, 1000L);
        pending.setStatus(WxPaymentStatus.PENDING);

        MockWxPayService payService = new MockWxPayService() {
            @Override
            public WxPayment getPayment(Long id) {
                return pending;
            }
        };
        MockWxRefundService svc = new MockWxRefundService(payService);

        WxRefundCreateRequest req = new WxRefundCreateRequest();
        req.setPaymentId(1L);
        req.setRefundAmountCents(1000L);
        req.setReason("x");

        assertThatThrownBy(() -> svc.createRefund(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("支付成功");
    }

    @Test
    @DisplayName("createRefund 拒绝退款金额超过原支付金额")
    void createRefund_rejectsAmountOverflow() {
        WxPayment payment = successPayment(2L, 5000L);
        MockWxPayService payService = new MockWxPayService() {
            @Override
            public WxPayment getPayment(Long id) {
                return payment;
            }
            @Override
            public boolean updateById(WxPayment entity) {
                return true;
            }
        };
        MockWxRefundService svc = new MockWxRefundService(payService) {
            @Override
            public boolean save(WxRefund entity) {
                return true;
            }
            @Override
            public WxRefund getById(Serializable id) {
                return null;
            }
        };

        WxRefundCreateRequest req = new WxRefundCreateRequest();
        req.setPaymentId(2L);
        req.setRefundAmountCents(9999L);
        req.setReason("超额退款");

        assertThatThrownBy(() -> svc.createRefund(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能超过");
    }
}
