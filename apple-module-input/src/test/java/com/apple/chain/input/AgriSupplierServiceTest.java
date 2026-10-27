package com.apple.chain.input;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.input.entity.AgriSupplier;
import com.apple.chain.input.mapper.AgriSupplierMapper;
import com.apple.chain.input.service.impl.AgriSupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("M2 AgriSupplierService 供应商审核状态机 单元测试")
class AgriSupplierServiceTest {

    @Mock
    private AgriSupplierMapper agriSupplierMapper;

    @Spy
    private AgriSupplierServiceImpl supplierService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(supplierService, "baseMapper", agriSupplierMapper);
    }

    // ---- factory helpers ----

    private AgriSupplier pendingSupplier() {
        AgriSupplier s = new AgriSupplier();
        s.setId(1L);
        s.setName("测试供应商");
        s.setStatus("PENDING");
        return s;
    }

    private AgriSupplier approvedSupplier() {
        AgriSupplier s = new AgriSupplier();
        s.setId(2L);
        s.setName("已通过供应商");
        s.setStatus("APPROVED");
        return s;
    }

    private AgriSupplier rejectedSupplier() {
        AgriSupplier s = new AgriSupplier();
        s.setId(3L);
        s.setName("已拒绝供应商");
        s.setStatus("REJECTED");
        s.setRejectReason("资质不合格");
        return s;
    }

    private AgriSupplier blacklistedSupplier() {
        AgriSupplier s = new AgriSupplier();
        s.setId(4L);
        s.setName("黑名单供应商");
        s.setStatus("BLACKLISTED");
        return s;
    }

    private void stubGetById(AgriSupplier supplier) {
        given(agriSupplierMapper.selectById(supplier.getId())).willReturn(supplier);
        given(agriSupplierMapper.updateById(any(AgriSupplier.class))).willReturn(1);
    }

    // ========== auditSupplier tests ==========

    @Test
    @DisplayName("auditSupplier: PENDING -> APPROVED (有效转换)")
    void auditSupplier_pendingToApproved() {
        AgriSupplier supplier = pendingSupplier();
        stubGetById(supplier);

        AgriSupplier result = supplierService.auditSupplier(1L, "APPROVE", null);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getAuditTime()).isNotNull();
        then(agriSupplierMapper).should().updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("auditSupplier: PENDING -> REJECTED (有效转换)")
    void auditSupplier_pendingToRejected() {
        AgriSupplier supplier = pendingSupplier();
        stubGetById(supplier);

        AgriSupplier result = supplierService.auditSupplier(1L, "REJECT", "资质不合格");

        assertThat(result.getStatus()).isEqualTo("REJECTED");
        assertThat(result.getRejectReason()).isEqualTo("资质不合格");
        assertThat(result.getAuditTime()).isNotNull();
        then(agriSupplierMapper).should().updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("auditSupplier: PENDING -> BLACKLISTED (有效转换)")
    void auditSupplier_pendingToBlacklisted() {
        AgriSupplier supplier = pendingSupplier();
        stubGetById(supplier);

        AgriSupplier result = supplierService.auditSupplier(1L, "BLACKLIST", "欺诈行为");

        assertThat(result.getStatus()).isEqualTo("BLACKLISTED");
        assertThat(result.getAuditTime()).isNotNull();
        then(agriSupplierMapper).should().updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("auditSupplier: APPROVED -> REJECTED (实现不校验来源状态, 覆盖写入)")
    void auditSupplier_approvedToRejected_overwrites() {
        AgriSupplier supplier = approvedSupplier();
        stubGetById(supplier);

        AgriSupplier result = supplierService.auditSupplier(2L, "REJECT", "重新拒绝");

        // Implementation does not guard status transitions -- auditSupplier always applies
        assertThat(result.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("auditSupplier: 无效决策抛出 BizException")
    void auditSupplier_invalidDecision_throws() {
        AgriSupplier supplier = pendingSupplier();
        given(agriSupplierMapper.selectById(1L)).willReturn(supplier);

        assertThatThrownBy(() -> supplierService.auditSupplier(1L, "INVALID", "无"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("无效的审核决策");
        then(agriSupplierMapper).should(never()).updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("auditSupplier: 供应商不存在抛出 BizException")
    void auditSupplier_notFound_throws() {
        given(agriSupplierMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> supplierService.auditSupplier(999L, "APPROVE", null))
                .isInstanceOf(BizException.class);
        then(agriSupplierMapper).should(never()).updateById(any(AgriSupplier.class));
    }

    // ========== reinstateSupplier tests ==========

    @Test
    @DisplayName("reinstateSupplier: REJECTED -> PENDING (有效转换)")
    void reinstateSupplier_rejectedToPending() {
        AgriSupplier supplier = rejectedSupplier();
        stubGetById(supplier);

        AgriSupplier result = supplierService.reinstateSupplier(3L);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getRejectReason()).isNull();
        then(agriSupplierMapper).should().updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("reinstateSupplier: APPROVED 状态不允许重新提交, 抛出 BizException")
    void reinstateSupplier_approvedStatus_throws() {
        AgriSupplier supplier = approvedSupplier();
        given(agriSupplierMapper.selectById(2L)).willReturn(supplier);

        assertThatThrownBy(() -> supplierService.reinstateSupplier(2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已拒绝的供应商可以重新提交审核");
        then(agriSupplierMapper).should(never()).updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("reinstateSupplier: PENDING 状态不允许重新提交, 抛出 BizException")
    void reinstateSupplier_pendingStatus_throws() {
        AgriSupplier supplier = pendingSupplier();
        given(agriSupplierMapper.selectById(1L)).willReturn(supplier);

        assertThatThrownBy(() -> supplierService.reinstateSupplier(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已拒绝的供应商可以重新提交审核");
        then(agriSupplierMapper).should(never()).updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("reinstateSupplier: BLACKLISTED 状态不允许重新提交, 抛出 BizException")
    void reinstateSupplier_blacklistedStatus_throws() {
        AgriSupplier supplier = blacklistedSupplier();
        given(agriSupplierMapper.selectById(4L)).willReturn(supplier);

        assertThatThrownBy(() -> supplierService.reinstateSupplier(4L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已拒绝的供应商可以重新提交审核");
        then(agriSupplierMapper).should(never()).updateById(any(AgriSupplier.class));
    }

    @Test
    @DisplayName("reinstateSupplier: 供应商不存在抛出 BizException")
    void reinstateSupplier_notFound_throws() {
        given(agriSupplierMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> supplierService.reinstateSupplier(999L))
                .isInstanceOf(BizException.class);
        then(agriSupplierMapper).should(never()).updateById(any(AgriSupplier.class));
    }
}
