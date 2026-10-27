package com.apple.chain.trade;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.mapper.TradeOrderMapper;
import com.apple.chain.trade.service.impl.TradeOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@DisplayName("M4 TradeOrderService 交易订单状态机 单元测试")
class TradeOrderServiceTest {

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    private TradeOrderServiceImpl orderService;

    /** In-memory store simulating DB for getById/updateById round-trip */
    private final Map<Long, TradeOrder> db = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        orderService = spy(new TradeOrderServiceImpl());
        ReflectionTestUtils.setField(orderService, "baseMapper", tradeOrderMapper);
        db.clear();
    }

    /** Register entity for selectById only (used in throw tests) */
    private void registerSelect(TradeOrder order) {
        db.put(order.getId(), order);
        given(tradeOrderMapper.selectById(order.getId())).willAnswer(inv ->
                db.get(inv.getArgument(0, Long.class))
        );
    }

    /** Register entity with full update simulation (used in happy-path tests) */
    private void registerFull(TradeOrder order) {
        registerSelect(order);
        doAnswer(inv -> {
            TradeOrder update = inv.getArgument(0);
            TradeOrder stored = db.get(update.getId());
            if (stored != null) {
                if (update.getOrderStatus() != null) stored.setOrderStatus(update.getOrderStatus());
                if (update.getPaymentStatus() != null) stored.setPaymentStatus(update.getPaymentStatus());
            }
            return 1;
        }).when(tradeOrderMapper).updateById(any(TradeOrder.class));
    }

    // ---- factory helpers ----

    private TradeOrder draftOrder() {
        TradeOrder o = new TradeOrder();
        o.setId(1L);
        o.setOrderNo("ORD202604110001");
        o.setFarmerId(100L);
        o.setBuyerId(200L);
        o.setVariety("红富士");
        o.setQuantity(new BigDecimal("500"));
        o.setUnitPrice(new BigDecimal("8.00"));
        o.setTotalAmount(new BigDecimal("4000.00"));
        o.setOrderStatus("DRAFT");
        o.setPaymentStatus("PENDING");
        return o;
    }

    private TradeOrder confirmedOrder() {
        TradeOrder o = draftOrder();
        o.setId(2L);
        o.setOrderNo("ORD202604110002");
        o.setOrderStatus("CONFIRMED");
        return o;
    }

    private TradeOrder deliveredOrder() {
        TradeOrder o = draftOrder();
        o.setId(3L);
        o.setOrderNo("ORD202604110003");
        o.setOrderStatus("DELIVERED");
        return o;
    }

    private TradeOrder completedOrder() {
        TradeOrder o = draftOrder();
        o.setId(4L);
        o.setOrderNo("ORD202604110004");
        o.setOrderStatus("COMPLETED");
        return o;
    }

    private TradeOrder cancelledOrder() {
        TradeOrder o = draftOrder();
        o.setId(5L);
        o.setOrderNo("ORD202604110005");
        o.setOrderStatus("CANCELLED");
        return o;
    }

    // ========== confirmOrder tests ==========

    @Test
    @DisplayName("confirmOrder: DRAFT -> CONFIRMED (有效确认)")
    void confirmOrder_draftToConfirmed() {
        TradeOrder order = draftOrder();
        registerFull(order);

        TradeOrder result = orderService.confirmOrder(1L);

        assertThat(result.getOrderStatus()).isEqualTo("CONFIRMED");
        then(tradeOrderMapper).should().updateById(any(TradeOrder.class));
    }

    @Test
    @DisplayName("confirmOrder: 非 DRAFT 状态抛出 BizException")
    void confirmOrder_nonDraft_throws() {
        TradeOrder order = confirmedOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.confirmOrder(2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有草稿状态可以确认");
        then(tradeOrderMapper).should(never()).updateById(any(TradeOrder.class));
    }

    // ========== deliverOrder tests ==========

    @Test
    @DisplayName("deliverOrder: CONFIRMED -> DELIVERED (有效发货)")
    void deliverOrder_confirmedToDelivered() {
        TradeOrder order = confirmedOrder();
        registerFull(order);

        TradeOrder result = orderService.deliverOrder(2L);

        assertThat(result.getOrderStatus()).isEqualTo("DELIVERED");
        then(tradeOrderMapper).should().updateById(any(TradeOrder.class));
    }

    @Test
    @DisplayName("deliverOrder: 非 CONFIRMED 状态抛出 BizException")
    void deliverOrder_nonConfirmed_throws() {
        TradeOrder order = draftOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.deliverOrder(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已确认订单可以标记发货");
        then(tradeOrderMapper).should(never()).updateById(any(TradeOrder.class));
    }

    // ========== completeOrder tests ==========

    @Test
    @DisplayName("completeOrder: DELIVERED -> COMPLETED (有效完成)")
    void completeOrder_deliveredToCompleted() {
        TradeOrder order = deliveredOrder();
        registerFull(order);

        TradeOrder result = orderService.completeOrder(3L);

        assertThat(result.getOrderStatus()).isEqualTo("COMPLETED");
        then(tradeOrderMapper).should().updateById(any(TradeOrder.class));
    }

    @Test
    @DisplayName("completeOrder: 非 DELIVERED 状态抛出 BizException")
    void completeOrder_nonDelivered_throws() {
        TradeOrder order = confirmedOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.completeOrder(2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已发货订单可以完成");
        then(tradeOrderMapper).should(never()).updateById(any(TradeOrder.class));
    }

    // ========== cancelOrder tests ==========

    @Test
    @DisplayName("cancelOrder: DRAFT -> CANCELLED (有效取消)")
    void cancelOrder_draftToCancelled() {
        TradeOrder order = draftOrder();
        registerFull(order);

        TradeOrder result = orderService.cancelOrder(1L);

        assertThat(result.getOrderStatus()).isEqualTo("CANCELLED");
        then(tradeOrderMapper).should().updateById(any(TradeOrder.class));
    }

    @Test
    @DisplayName("cancelOrder: CONFIRMED -> CANCELLED (有效取消)")
    void cancelOrder_confirmedToCancelled() {
        TradeOrder order = confirmedOrder();
        registerFull(order);

        TradeOrder result = orderService.cancelOrder(2L);

        assertThat(result.getOrderStatus()).isEqualTo("CANCELLED");
        then(tradeOrderMapper).should().updateById(any(TradeOrder.class));
    }

    @Test
    @DisplayName("cancelOrder: DELIVERED -> CANCELLED (有效取消, 已发货可取消)")
    void cancelOrder_deliveredToCancelled() {
        TradeOrder order = deliveredOrder();
        registerFull(order);

        TradeOrder result = orderService.cancelOrder(3L);

        assertThat(result.getOrderStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("cancelOrder: COMPLETED 状态不允许取消, 抛出 BizException")
    void cancelOrder_completed_throws() {
        TradeOrder order = completedOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.cancelOrder(4L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("当前状态不允许取消");
        then(tradeOrderMapper).should(never()).updateById(any(TradeOrder.class));
    }

    @Test
    @DisplayName("cancelOrder: CANCELLED 状态不允许重复取消, 抛出 BizException")
    void cancelOrder_alreadyCancelled_throws() {
        TradeOrder order = cancelledOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.cancelOrder(5L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("当前状态不允许取消");
        then(tradeOrderMapper).should(never()).updateById(any(TradeOrder.class));
    }

    // ========== full lifecycle test ==========

    @Test
    @DisplayName("完整生命周期: DRAFT -> CONFIRMED -> DELIVERED -> COMPLETED")
    void fullLifecycle_happyPath() {
        TradeOrder order = draftOrder();
        registerFull(order);

        // DRAFT -> CONFIRMED
        TradeOrder confirmed = orderService.confirmOrder(1L);
        assertThat(confirmed.getOrderStatus()).isEqualTo("CONFIRMED");

        // CONFIRMED -> DELIVERED
        TradeOrder delivered = orderService.deliverOrder(1L);
        assertThat(delivered.getOrderStatus()).isEqualTo("DELIVERED");

        // DELIVERED -> COMPLETED
        TradeOrder completed = orderService.completeOrder(1L);
        assertThat(completed.getOrderStatus()).isEqualTo("COMPLETED");

        // CANCELLED should fail on COMPLETED
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("当前状态不允许取消");
    }

    // ========== getOrderDetail tests ==========

    @Test
    @DisplayName("getOrderDetail: 订单不存在抛出 BizException")
    void getOrderDetail_notFound_throws() {
        given(tradeOrderMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> orderService.getOrderDetail(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单不存在");
    }

    // ========== reverse transition tests ==========

    @Test
    @DisplayName("状态不可逆: CONFIRMED 不能直接 confirm (已是 CONFIRMED)")
    void reverseTransition_confirmedCannotConfirmAgain() {
        TradeOrder order = confirmedOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.confirmOrder(2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有草稿状态可以确认");
    }

    @Test
    @DisplayName("状态不可逆: DELIVERED 不能 confirm (跳过 CONFIRMED)")
    void reverseTransition_deliveredCannotConfirm() {
        TradeOrder order = deliveredOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.confirmOrder(3L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有草稿状态可以确认");
    }

    @Test
    @DisplayName("状态不可逆: DELIVERED 不能 deliver (已是 DELIVERED)")
    void reverseTransition_deliveredCannotDeliverAgain() {
        TradeOrder order = deliveredOrder();
        registerSelect(order);

        assertThatThrownBy(() -> orderService.deliverOrder(3L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已确认订单可以标记发货");
    }
}
