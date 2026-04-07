package com.apple.chain.trade;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trade.entity.TradeMatch;
import com.apple.chain.trade.entity.TradeNegotiation;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.mapper.TradeMatchMapper;
import com.apple.chain.trade.mapper.TradeNegotiationMapper;
import com.apple.chain.trade.mapper.TradeOrderMapper;
import com.apple.chain.trade.service.impl.NegotiationServiceImpl;
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
@DisplayName("M7 NegotiationService 单元测试")
class NegotiationServiceTest {

    @Mock private TradeNegotiationMapper negotiationMapper;
    @Mock private TradeMatchMapper tradeMatchMapper;
    @Mock private TradeOrderMapper tradeOrderMapper;

    @InjectMocks
    private NegotiationServiceImpl service;

    private TradeNegotiation ongoing(Long id, Long supplyUid, Long demandUid, Long lastOfferBy) {
        TradeNegotiation neg = new TradeNegotiation();
        neg.setId(id);
        neg.setMatchId(1L);
        neg.setSupplyUserId(supplyUid);
        neg.setDemandUserId(demandUid);
        neg.setCurrentPrice(BigDecimal.valueOf(5.0));
        neg.setCurrentQuantity(BigDecimal.valueOf(1000));
        neg.setLastOfferBy(lastOfferBy);
        neg.setStatus(TradeNegotiation.STATUS_ONGOING);
        return neg;
    }

    @Test
    @DisplayName("start 创建议价 + 更新 match 状态")
    void start_succeeds() {
        TradeMatch match = new TradeMatch();
        match.setId(1L);
        match.setStatus(TradeMatch.STATUS_CANDIDATE);
        given(tradeMatchMapper.selectById(1L)).willReturn(match);
        given(negotiationMapper.insert(any(TradeNegotiation.class))).willReturn(1);
        given(tradeMatchMapper.updateById(any(TradeMatch.class))).willReturn(1);

        TradeNegotiation neg = service.start(1L, 100L, 200L,
                BigDecimal.valueOf(5.0), BigDecimal.valueOf(1000));

        assertThat(neg.getStatus()).isEqualTo(TradeNegotiation.STATUS_ONGOING);
        assertThat(neg.getCurrentPrice()).isEqualByComparingTo("5.0");
        assertThat(neg.getLastOfferBy()).isEqualTo(100L);
        assertThat(match.getStatus()).isEqualTo(TradeMatch.STATUS_NEGOTIATE);
    }

    @Test
    @DisplayName("start match 不存在抛错")
    void start_matchMissing() {
        given(tradeMatchMapper.selectById(99L)).willReturn(null);
        assertThatThrownBy(() -> service.start(99L, 1L, 2L,
                BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("offer 更新当前价格 + 切换 lastOfferBy")
    void offer_updatesState() {
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        given(negotiationMapper.selectById(10L)).willReturn(neg);
        given(negotiationMapper.updateById(any(TradeNegotiation.class))).willReturn(1);

        TradeNegotiation r = service.offer(10L, 200L, BigDecimal.valueOf(6.5), null);

        assertThat(r.getCurrentPrice()).isEqualByComparingTo("6.5");
        assertThat(r.getLastOfferBy()).isEqualTo(200L);
    }

    @Test
    @DisplayName("offer 非会话用户拒绝")
    void offer_strangerForbidden() {
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        given(negotiationMapper.selectById(10L)).willReturn(neg);
        assertThatThrownBy(() -> service.offer(10L, 999L, BigDecimal.ONE, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不在");
    }

    @Test
    @DisplayName("offer 价格非正抛错")
    void offer_invalidPrice() {
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        given(negotiationMapper.selectById(10L)).willReturn(neg);
        assertThatThrownBy(() -> service.offer(10L, 200L, BigDecimal.ZERO, null))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("accept 创建 trade order，状态推进")
    void accept_createsOrder() {
        // last offer was supply user 100; demand user 200 accepts
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        given(negotiationMapper.selectById(10L)).willReturn(neg);
        given(tradeMatchMapper.selectById(1L)).willReturn(new TradeMatch());
        given(tradeOrderMapper.nextSeq(anyString())).willReturn(1);
        given(tradeOrderMapper.insert(any(TradeOrder.class))).willReturn(1);
        given(negotiationMapper.updateById(any(TradeNegotiation.class))).willReturn(1);
        given(tradeMatchMapper.updateById(any(TradeMatch.class))).willReturn(1);

        TradeOrder order = service.accept(10L, 200L);

        assertThat(order).isNotNull();
        assertThat(order.getOrderNo()).startsWith("ORD");
        assertThat(order.getBuyerId()).isEqualTo(200L);
        assertThat(order.getFarmerId()).isEqualTo(100L);
        assertThat(order.getUnitPrice()).isEqualByComparingTo("5.0");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("5000.0");
        assertThat(order.getOrderStatus()).isEqualTo("DRAFT");
        assertThat(neg.getStatus()).isEqualTo(TradeNegotiation.STATUS_DEALT);
    }

    @Test
    @DisplayName("accept 不能接受自己的报价")
    void accept_cannotSelfAccept() {
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        given(negotiationMapper.selectById(10L)).willReturn(neg);
        assertThatThrownBy(() -> service.accept(10L, 100L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能接受自己");
    }

    @Test
    @DisplayName("cancel 切换状态为 CANCELED")
    void cancel_changesState() {
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        given(negotiationMapper.selectById(10L)).willReturn(neg);
        given(negotiationMapper.updateById(any(TradeNegotiation.class))).willReturn(1);

        TradeNegotiation r = service.cancel(10L, 100L);
        assertThat(r.getStatus()).isEqualTo(TradeNegotiation.STATUS_CANCELED);
    }

    @Test
    @DisplayName("非 ONGOING 议价拒绝任何操作")
    void nonOngoing_isRejected() {
        TradeNegotiation neg = ongoing(10L, 100L, 200L, 100L);
        neg.setStatus(TradeNegotiation.STATUS_DEALT);
        given(negotiationMapper.selectById(10L)).willReturn(neg);

        assertThatThrownBy(() -> service.offer(10L, 200L, BigDecimal.ONE, null))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("非 ONGOING");
    }
}
