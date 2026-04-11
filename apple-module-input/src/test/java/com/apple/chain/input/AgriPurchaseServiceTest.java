package com.apple.chain.input;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.input.entity.AgriInventory;
import com.apple.chain.input.entity.AgriPurchase;
import com.apple.chain.input.mapper.AgriInventoryMapper;
import com.apple.chain.input.mapper.AgriPurchaseMapper;
import com.apple.chain.input.service.impl.AgriPurchaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
@DisplayName("M2 AgriPurchaseService 采购审批与库存 单元测试")
class AgriPurchaseServiceTest {

    @Mock
    private AgriPurchaseMapper agriPurchaseMapper;

    @Mock
    private AgriInventoryMapper agriInventoryMapper;

    private AgriPurchaseServiceImpl purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = spy(new AgriPurchaseServiceImpl(agriInventoryMapper));
        ReflectionTestUtils.setField(purchaseService, "baseMapper", agriPurchaseMapper);
    }

    // ---- factory helpers ----

    private AgriPurchase pendingPurchase() {
        AgriPurchase p = new AgriPurchase();
        p.setId(1L);
        p.setPurchaseNo("PO202604110001");
        p.setProductId(100L);
        p.setProductName("有机肥料");
        p.setQuantity(new BigDecimal("50"));
        p.setUnit("kg");
        p.setUnitPrice(new BigDecimal("10.00"));
        p.setStatus("PENDING");
        return p;
    }

    private AgriPurchase approvedPurchase() {
        AgriPurchase p = new AgriPurchase();
        p.setId(2L);
        p.setPurchaseNo("PO202604110002");
        p.setProductId(100L);
        p.setProductName("有机肥料");
        p.setQuantity(new BigDecimal("50"));
        p.setUnit("kg");
        p.setUnitPrice(new BigDecimal("10.00"));
        p.setStatus("APPROVED");
        return p;
    }

    private AgriInventory existingInventory(BigDecimal stock) {
        AgriInventory inv = new AgriInventory();
        inv.setId(10L);
        inv.setProductId(100L);
        inv.setProductName("有机肥料");
        inv.setStockQuantity(stock);
        inv.setUnit("kg");
        inv.setStatus("NORMAL");
        return inv;
    }

    private void stubPurchaseUpdate() {
        given(agriPurchaseMapper.updateById(any(AgriPurchase.class))).willReturn(1);
    }

    // ========== approvePurchase tests ==========

    @Test
    @DisplayName("approvePurchase: PENDING -> APPROVED (有效审批)")
    void approvePurchase_pendingToApproved() {
        AgriPurchase purchase = pendingPurchase();
        given(agriPurchaseMapper.selectById(1L)).willReturn(purchase);
        stubPurchaseUpdate();

        AgriPurchase result = purchaseService.approvePurchase(1L);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        then(agriPurchaseMapper).should().updateById(any(AgriPurchase.class));
    }

    @Test
    @DisplayName("approvePurchase: 非 PENDING 状态抛出 BizException")
    void approvePurchase_nonPending_throws() {
        AgriPurchase purchase = approvedPurchase();
        given(agriPurchaseMapper.selectById(2L)).willReturn(purchase);

        assertThatThrownBy(() -> purchaseService.approvePurchase(2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有待审核的采购单可以审批");
        then(agriPurchaseMapper).should(never()).updateById(any(AgriPurchase.class));
    }

    @Test
    @DisplayName("approvePurchase: 采购单不存在抛出 BizException")
    void approvePurchase_notFound_throws() {
        given(agriPurchaseMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> purchaseService.approvePurchase(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("采购记录不存在");
        then(agriPurchaseMapper).should(never()).updateById(any(AgriPurchase.class));
    }

    // ========== receivePurchase tests ==========

    @Test
    @DisplayName("receivePurchase: APPROVED -> RECEIVED 并自动增加库存")
    void receivePurchase_approvedToReceived_withInventory() {
        AgriPurchase purchase = approvedPurchase();
        given(agriPurchaseMapper.selectById(2L)).willReturn(purchase);
        stubPurchaseUpdate();

        AgriInventory inventory = existingInventory(new BigDecimal("30"));
        given(agriInventoryMapper.selectOne(any())).willReturn(inventory);
        given(agriInventoryMapper.updateById(any(AgriInventory.class))).willReturn(1);

        AgriPurchase result = purchaseService.receivePurchase(2L);

        assertThat(result.getStatus()).isEqualTo("RECEIVED");
        // stockQuantity: 30 + 50 = 80
        assertThat(inventory.getStockQuantity()).isEqualByComparingTo("80");
        assertThat(inventory.getStatus()).isEqualTo("NORMAL");
        then(agriInventoryMapper).should().updateById(inventory);
    }

    @Test
    @DisplayName("receivePurchase: 空库存首次入库, 设置为采购数量")
    void receivePurchase_emptyInventory_setsQuantity() {
        AgriPurchase purchase = approvedPurchase();
        given(agriPurchaseMapper.selectById(2L)).willReturn(purchase);
        stubPurchaseUpdate();

        AgriInventory inventory = existingInventory(null);
        given(agriInventoryMapper.selectOne(any())).willReturn(inventory);
        given(agriInventoryMapper.updateById(any(AgriInventory.class))).willReturn(1);

        purchaseService.receivePurchase(2L);

        assertThat(inventory.getStockQuantity()).isEqualByComparingTo("50");
        then(agriInventoryMapper).should().updateById(inventory);
    }

    @Test
    @DisplayName("receivePurchase: 非 APPROVED 状态抛出 BizException")
    void receivePurchase_nonApproved_throws() {
        AgriPurchase purchase = pendingPurchase();
        given(agriPurchaseMapper.selectById(1L)).willReturn(purchase);

        assertThatThrownBy(() -> purchaseService.receivePurchase(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已审批的采购单可以确认收货");
        then(agriInventoryMapper).should(never()).updateById(any(AgriInventory.class));
    }

    @Test
    @DisplayName("receivePurchase: 库存超出上限, 状态变为 OVERSTOCKED")
    void receivePurchase_overstockedStatus() {
        AgriPurchase purchase = approvedPurchase();
        given(agriPurchaseMapper.selectById(2L)).willReturn(purchase);
        stubPurchaseUpdate();

        AgriInventory inventory = existingInventory(new BigDecimal("90"));
        inventory.setMaxLevel(new BigDecimal("100"));
        given(agriInventoryMapper.selectOne(any())).willReturn(inventory);
        given(agriInventoryMapper.updateById(any(AgriInventory.class))).willReturn(1);

        purchaseService.receivePurchase(2L);

        // 90 + 50 = 140 > 100 -> OVERSTOCKED
        assertThat(inventory.getStatus()).isEqualTo("OVERSTOCKED");
    }

    @Test
    @DisplayName("receivePurchase: 库存低于预警, 状态变为 LOW")
    void receivePurchase_lowStockStatus() {
        AgriPurchase purchase = new AgriPurchase();
        purchase.setId(5L);
        purchase.setProductId(100L);
        purchase.setQuantity(new BigDecimal("3"));
        purchase.setStatus("APPROVED");
        given(agriPurchaseMapper.selectById(5L)).willReturn(purchase);
        stubPurchaseUpdate();

        AgriInventory inventory = existingInventory(new BigDecimal("0"));
        inventory.setWarningLevel(new BigDecimal("10"));
        given(agriInventoryMapper.selectOne(any())).willReturn(inventory);
        given(agriInventoryMapper.updateById(any(AgriInventory.class))).willReturn(1);

        purchaseService.receivePurchase(5L);

        // 0 + 3 = 3 <= 10 -> LOW
        assertThat(inventory.getStatus()).isEqualTo("LOW");
    }

    // ========== cancelPurchase tests ==========

    @Test
    @DisplayName("cancelPurchase: PENDING -> CANCELLED (有效取消)")
    void cancelPurchase_pendingToCancelled() {
        AgriPurchase purchase = pendingPurchase();
        given(agriPurchaseMapper.selectById(1L)).willReturn(purchase);
        stubPurchaseUpdate();

        AgriPurchase result = purchaseService.cancelPurchase(1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        then(agriPurchaseMapper).should().updateById(any(AgriPurchase.class));
    }

    @Test
    @DisplayName("cancelPurchase: 非 PENDING 状态抛出 BizException")
    void cancelPurchase_nonPending_throws() {
        AgriPurchase purchase = approvedPurchase();
        given(agriPurchaseMapper.selectById(2L)).willReturn(purchase);

        assertThatThrownBy(() -> purchaseService.cancelPurchase(2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有待审核的采购单可以取消");
        then(agriPurchaseMapper).should(never()).updateById(any(AgriPurchase.class));
    }

    @Test
    @DisplayName("cancelPurchase: 采购单不存在抛出 BizException")
    void cancelPurchase_notFound_throws() {
        given(agriPurchaseMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> purchaseService.cancelPurchase(999L))
                .isInstanceOf(BizException.class);
    }
}
