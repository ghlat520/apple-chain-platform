package com.apple.chain.trade;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trade.entity.QualityInspection;
import com.apple.chain.trade.mapper.QualityInspectionMapper;
import com.apple.chain.trade.service.impl.QualityInspectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;
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
@DisplayName("M4 QualityInspectionService 质检判定 单元测试")
class QualityInspectionServiceTest {

    @Mock
    private QualityInspectionMapper qualityInspectionMapper;

    private QualityInspectionServiceImpl inspectionService;

    /** In-memory store simulating DB for getById/updateById round-trip */
    private final Map<Long, QualityInspection> db = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        inspectionService = spy(new QualityInspectionServiceImpl());
        ReflectionTestUtils.setField(inspectionService, "baseMapper", qualityInspectionMapper);
        db.clear();
    }

    /** Register entity for selectById only (used in throw tests) */
    private void registerSelect(QualityInspection qi) {
        db.put(qi.getId(), qi);
        given(qualityInspectionMapper.selectById(qi.getId())).willAnswer(inv ->
                db.get(inv.getArgument(0, Long.class))
        );
    }

    /** Register entity with full update simulation (used in happy-path tests) */
    private void registerFull(QualityInspection qi) {
        registerSelect(qi);
        doAnswer(inv -> {
            QualityInspection update = inv.getArgument(0);
            QualityInspection stored = db.get(update.getId());
            if (stored != null) {
                if (update.getStatus() != null) stored.setStatus(update.getStatus());
                if (update.getResult() != null) stored.setResult(update.getResult());
                if (update.getBrixValue() != null) stored.setBrixValue(update.getBrixValue());
                if (update.getFirmnessValue() != null) stored.setFirmnessValue(update.getFirmnessValue());
                if (update.getColorScore() != null) stored.setColorScore(update.getColorScore());
                if (update.getDefectRate() != null) stored.setDefectRate(update.getDefectRate());
                if (update.getInspectorName() != null) stored.setInspectorName(update.getInspectorName());
                if (update.getReportUrl() != null) stored.setReportUrl(update.getReportUrl());
                if (update.getRemark() != null) stored.setRemark(update.getRemark());
            }
            return 1;
        }).when(qualityInspectionMapper).updateById(any(QualityInspection.class));
    }

    // ---- factory helpers ----

    private QualityInspection pendingInspection() {
        QualityInspection qi = new QualityInspection();
        qi.setId(1L);
        qi.setInspectionNo("QI202604110001");
        qi.setStatus("PENDING");
        qi.setOrderId(100L);
        return qi;
    }

    private QualityInspection inspectedInspection() {
        QualityInspection qi = new QualityInspection();
        qi.setId(2L);
        qi.setInspectionNo("QI202604110002");
        qi.setStatus("INSPECTED");
        qi.setResult("PASS");
        qi.setOrderId(101L);
        return qi;
    }

    private QualityInspection acceptedInspection() {
        QualityInspection qi = new QualityInspection();
        qi.setId(3L);
        qi.setInspectionNo("QI202604110003");
        qi.setStatus("ACCEPTED");
        qi.setResult("PASS");
        return qi;
    }

    private QualityInspection passData() {
        QualityInspection data = new QualityInspection();
        data.setBrixValue(new BigDecimal("14"));
        data.setFirmnessValue(new BigDecimal("8"));
        data.setColorScore(new BigDecimal("80"));
        data.setDefectRate(new BigDecimal("3"));
        data.setInspectorName("张检验员");
        return data;
    }

    // ========== inspect tests: PASS/FAIL thresholds ==========

    @Test
    @DisplayName("inspect: brix>=12, firmness>=6, color>=70, defect<=5 -> PASS")
    void inspect_allCriteriaMet_pass() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection result = inspectionService.inspect(1L, passData());

        assertThat(result.getStatus()).isEqualTo("INSPECTED");
        assertThat(result.getResult()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("inspect: brix < 12 -> FAIL")
    void inspect_brixTooLow_fail() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = passData();
        data.setBrixValue(new BigDecimal("10"));

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getStatus()).isEqualTo("INSPECTED");
        assertThat(result.getResult()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("inspect: firmness < 6 -> FAIL")
    void inspect_firmnessTooLow_fail() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = passData();
        data.setFirmnessValue(new BigDecimal("4"));

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getStatus()).isEqualTo("INSPECTED");
        assertThat(result.getResult()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("inspect: color < 70 -> FAIL")
    void inspect_colorTooLow_fail() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = passData();
        data.setColorScore(new BigDecimal("60"));

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getStatus()).isEqualTo("INSPECTED");
        assertThat(result.getResult()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("inspect: defect > 5 -> FAIL")
    void inspect_defectTooHigh_fail() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = passData();
        data.setDefectRate(new BigDecimal("8"));

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getStatus()).isEqualTo("INSPECTED");
        assertThat(result.getResult()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("inspect: 边界值 brix=12, firmness=6, color=70, defect=5 -> PASS")
    void inspect_boundaryValues_pass() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = new QualityInspection();
        data.setBrixValue(new BigDecimal("12"));
        data.setFirmnessValue(new BigDecimal("6"));
        data.setColorScore(new BigDecimal("70"));
        data.setDefectRate(new BigDecimal("5"));
        data.setInspectorName("边界测试");

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getResult()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("inspect: 多项不合格同时 FAIL")
    void inspect_multipleFailures_fail() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = new QualityInspection();
        data.setBrixValue(new BigDecimal("8"));
        data.setFirmnessValue(new BigDecimal("3"));
        data.setColorScore(new BigDecimal("50"));
        data.setDefectRate(new BigDecimal("10"));

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getResult()).isEqualTo("FAIL");
    }

    @Test
    @DisplayName("inspect: 指标为 null 时不判定该项")
    void inspect_nullValues_noPenalty() {
        QualityInspection existing = pendingInspection();
        registerFull(existing);

        QualityInspection data = new QualityInspection();
        data.setBrixValue(new BigDecimal("14"));
        data.setFirmnessValue(new BigDecimal("8"));
        data.setColorScore(new BigDecimal("80"));
        data.setDefectRate(null);
        data.setInspectorName("空值测试");

        QualityInspection result = inspectionService.inspect(1L, data);

        assertThat(result.getResult()).isEqualTo("PASS");
    }

    @Test
    @DisplayName("inspect: 非 PENDING 状态抛出 BizException")
    void inspect_nonPending_throws() {
        QualityInspection existing = inspectedInspection();
        registerSelect(existing);

        assertThatThrownBy(() -> inspectionService.inspect(2L, passData()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有待检状态可以录入检验结果");
        then(qualityInspectionMapper).should(never()).updateById(any(QualityInspection.class));
    }

    // ========== accept tests ==========

    @Test
    @DisplayName("accept: INSPECTED -> ACCEPTED (有效验收)")
    void accept_inspectedToAccepted() {
        QualityInspection existing = inspectedInspection();
        registerFull(existing);

        QualityInspection result = inspectionService.accept(2L);

        assertThat(result.getStatus()).isEqualTo("ACCEPTED");
        then(qualityInspectionMapper).should().updateById(any(QualityInspection.class));
    }

    @Test
    @DisplayName("accept: 非 INSPECTED 状态抛出 BizException")
    void accept_nonInspected_throws() {
        QualityInspection existing = pendingInspection();
        registerSelect(existing);

        assertThatThrownBy(() -> inspectionService.accept(1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已检状态可以验收");
        then(qualityInspectionMapper).should(never()).updateById(any(QualityInspection.class));
    }

    @Test
    @DisplayName("accept: 已验收状态不允许重复验收, 抛出 BizException")
    void accept_alreadyAccepted_throws() {
        QualityInspection existing = acceptedInspection();
        registerSelect(existing);

        assertThatThrownBy(() -> inspectionService.accept(3L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已检状态可以验收");
    }

    // ========== dispute tests ==========

    @Test
    @DisplayName("dispute: INSPECTED -> DISPUTED (有效争议)")
    void dispute_inspectedToDisputed() {
        QualityInspection existing = inspectedInspection();
        registerFull(existing);

        QualityInspection result = inspectionService.dispute(2L, "检测数据有误");

        assertThat(result.getStatus()).isEqualTo("DISPUTED");
        then(qualityInspectionMapper).should().updateById(any(QualityInspection.class));
    }

    @Test
    @DisplayName("dispute: 非 INSPECTED 状态抛出 BizException")
    void dispute_nonInspected_throws() {
        QualityInspection existing = pendingInspection();
        registerSelect(existing);

        assertThatThrownBy(() -> inspectionService.dispute(1L, "理由"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已检状态可以发起争议");
        then(qualityInspectionMapper).should(never()).updateById(any(QualityInspection.class));
    }

    @Test
    @DisplayName("dispute: 已争议状态不允许重复发起, 抛出 BizException")
    void dispute_alreadyDisputed_throws() {
        QualityInspection qi = new QualityInspection();
        qi.setId(5L);
        qi.setStatus("DISPUTED");
        registerSelect(qi);

        assertThatThrownBy(() -> inspectionService.dispute(5L, "再次争议"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("只有已检状态可以发起争议");
    }

    // ========== getDetail tests ==========

    @Test
    @DisplayName("getDetail: 质检记录不存在抛出 BizException")
    void getDetail_notFound_throws() {
        given(qualityInspectionMapper.selectById(999L)).willReturn(null);

        assertThatThrownBy(() -> inspectionService.getDetail(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("质检记录不存在");
    }
}
