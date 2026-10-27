package com.apple.chain.trade.wxpay;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trade.wxpay.dto.WxPayCreateRequest;
import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.apple.chain.trade.wxpay.enums.WxPaymentStatus;
import com.apple.chain.trade.wxpay.service.MockWxPayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 单元测试 —— 通过匿名子类绕开 MyBatis-Plus ServiceImpl 的 baseMapper 依赖，
 * 只验证 MockWxPayService 自身的业务逻辑（编号/状态/幂等）。
 */
@DisplayName("MockWxPayService 单元测试")
class MockWxPayServiceTest {

    @Test
    @DisplayName("createPayment 生成 Mock prepayId / outTradeNo，初始状态 PENDING")
    void createPayment_initialPending() {
        MockWxPayService svc = new MockWxPayService() {
            @Override
            public boolean save(WxPayment entity) {
                entity.setId(1L);
                return true;
            }
        };
        WxPayCreateRequest req = new WxPayCreateRequest();
        req.setOrderId(1001L);
        req.setAmountCents(12800L);
        req.setDescription("烟台红富士 100kg");

        WxPayment p = svc.createPayment(req);

        assertThat(p.getOutTradeNo()).startsWith("WXPAY");
        assertThat(p.getPrepayId()).startsWith("wx");
        assertThat(p.getPayMethod()).isEqualTo("WECHAT");
        assertThat(p.getStatus()).isEqualTo(WxPaymentStatus.PENDING);
        assertThat(p.getAmountCents()).isEqualTo(12800L);
        assertThat(p.getTransactionId()).isNull();
        assertThat(p.getPayTime()).isNull();
    }

    @Test
    @DisplayName("createPayment 金额非正数抛 BizException")
    void createPayment_rejectsNonPositiveAmount() {
        MockWxPayService svc = new MockWxPayService();
        WxPayCreateRequest req = new WxPayCreateRequest();
        req.setOrderId(1L);
        req.setAmountCents(0L);

        assertThatThrownBy(() -> svc.createPayment(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("支付金额");
    }

    @Test
    @DisplayName("queryPayment 终态幂等（SUCCESS 再次查询不会改变状态）")
    void queryPayment_terminalIsIdempotent() {
        WxPayment existing = new WxPayment();
        existing.setId(10L);
        existing.setStatus(WxPaymentStatus.SUCCESS);
        existing.setTransactionId("42000existing");

        MockWxPayService svc = new MockWxPayService() {
            @Override
            public WxPayment getById(Serializable id) {
                return existing;
            }
        };

        WxPayment p = svc.queryPayment(10L);
        assertThat(p.getStatus()).isEqualTo(WxPaymentStatus.SUCCESS);
        assertThat(p.getTransactionId()).isEqualTo("42000existing");
    }
}
