package com.apple.chain.trace;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trace.entity.AnomalyTrace;
import com.apple.chain.trace.mapper.AnomalyTraceMapper;
import com.apple.chain.trace.service.impl.AnomalyTraceServiceImpl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * Unit tests for AnomalyTraceServiceImpl state machine.
 * Verifies OPEN -> INVESTIGATING -> RESOLVED transitions and impact analysis.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("异常追溯服务单元测试")
class AnomalyTraceServiceTest {

    @Mock
    private AnomalyTraceMapper anomalyTraceMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private AnomalyTraceServiceImpl anomalyTraceService;

    private AnomalyTrace openAnomaly;

    @BeforeEach
    void setUp() throws Exception {
        anomalyTraceService = new AnomalyTraceServiceImpl(jdbcTemplate);
        // ServiceImpl.baseMapper is protected; inject mock via reflection
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(anomalyTraceService, anomalyTraceMapper);

        openAnomaly = new AnomalyTrace();
        openAnomaly.setId(1L);
        openAnomaly.setTraceCode("TC20260401001");
        openAnomaly.setAnomalyType("PESTICIDE_EXCESS");
        openAnomaly.setSeverity("HIGH");
        openAnomaly.setDescription("农药残留超标");
        openAnomaly.setStatus("OPEN");
    }

    // ====================================================================== //
    //  reportAnomaly
    // ====================================================================== //

    @Nested
    @DisplayName("reportAnomaly 上报异常")
    class ReportAnomaly {

        @Test
        @DisplayName("上报后状态为 OPEN")
        void report_setsStatusToOpen() {
            given(anomalyTraceMapper.insert(any(AnomalyTrace.class))).willReturn(1);

            AnomalyTrace input = new AnomalyTrace();
            input.setAnomalyType("TEMP_VIOLATION");
            input.setDescription("温度超标");

            AnomalyTrace result = anomalyTraceService.reportAnomaly(input);

            assertThat(result.getStatus()).isEqualTo("OPEN");
            then(anomalyTraceMapper).should().insert(any(AnomalyTrace.class));
        }

        @Test
        @DisplayName("上报时忽略传入的 status 字段，强制设为 OPEN")
        void report_ignoresInputStatus() {
            given(anomalyTraceMapper.insert(any(AnomalyTrace.class))).willReturn(1);

            AnomalyTrace input = new AnomalyTrace();
            input.setAnomalyType("QUALITY_FAIL");
            input.setStatus("RESOLVED"); // malicious override attempt

            AnomalyTrace result = anomalyTraceService.reportAnomaly(input);

            assertThat(result.getStatus()).isEqualTo("OPEN");
        }
    }

    // ====================================================================== //
    //  investigate
    // ====================================================================== //

    @Nested
    @DisplayName("investigate 开始调查")
    class Investigate {

        @Test
        @DisplayName("OPEN -> INVESTIGATING 转换成功")
        void investigate_openToInvestigating_success() {
            AnomalyTrace updated = new AnomalyTrace();
            updated.setId(1L);
            updated.setStatus("INVESTIGATING");

            given(anomalyTraceMapper.selectById(1L))
                    .willReturn(openAnomaly)   // getAnomalyDetail call
                    .willReturn(updated);       // getById call after update
            given(anomalyTraceMapper.updateById(any(AnomalyTrace.class))).willReturn(1);

            AnomalyTrace result = anomalyTraceService.investigate(1L);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("INVESTIGATING");
            then(anomalyTraceMapper).should().updateById(any(AnomalyTrace.class));
        }

        @Test
        @DisplayName("INVESTIGATING 状态不可再次调查 - 抛出 BizException")
        void investigate_alreadyInvestigating_throwsBizException() {
            AnomalyTrace investigating = new AnomalyTrace();
            investigating.setId(2L);
            investigating.setStatus("INVESTIGATING");

            given(anomalyTraceMapper.selectById(2L)).willReturn(investigating);

            assertThatThrownBy(() -> anomalyTraceService.investigate(2L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("OPEN");
        }

        @Test
        @DisplayName("RESOLVED 状态不可调查 - 抛出 BizException")
        void investigate_alreadyResolved_throwsBizException() {
            AnomalyTrace resolved = new AnomalyTrace();
            resolved.setId(3L);
            resolved.setStatus("RESOLVED");

            given(anomalyTraceMapper.selectById(3L)).willReturn(resolved);

            assertThatThrownBy(() -> anomalyTraceService.investigate(3L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("OPEN");
        }

        @Test
        @DisplayName("不存在的 ID - 抛出 BizException")
        void investigate_nonExistent_throwsBizException() {
            given(anomalyTraceMapper.selectById(999L)).willReturn(null);

            assertThatThrownBy(() -> anomalyTraceService.investigate(999L))
                    .isInstanceOf(BizException.class);
        }
    }

    // ====================================================================== //
    //  resolve
    // ====================================================================== //

    @Nested
    @DisplayName("resolve 解决异常")
    class Resolve {

        @Test
        @DisplayName("INVESTIGATING -> RESOLVED 转换成功，设置 rootCause 和 resolvedBy")
        void resolve_investigatingToResolved_success() {
            AnomalyTrace investigating = new AnomalyTrace();
            investigating.setId(4L);
            investigating.setStatus("INVESTIGATING");

            AnomalyTrace afterUpdate = new AnomalyTrace();
            afterUpdate.setId(4L);
            afterUpdate.setStatus("RESOLVED");
            afterUpdate.setRootCauseAnalysis("农药喷洒时间过短，未达到安全间隔期");
            afterUpdate.setResolvedBy("张农艺师");
            afterUpdate.setResolvedTime(LocalDateTime.now());

            given(anomalyTraceMapper.selectById(4L))
                    .willReturn(investigating)
                    .willReturn(afterUpdate);
            given(anomalyTraceMapper.updateById(any(AnomalyTrace.class))).willReturn(1);

            AnomalyTrace result = anomalyTraceService.resolve(4L, "农药喷洒时间过短，未达到安全间隔期", "张农艺师");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("RESOLVED");
            assertThat(result.getRootCauseAnalysis()).isEqualTo("农药喷洒时间过短，未达到安全间隔期");
            assertThat(result.getResolvedBy()).isEqualTo("张农艺师");
        }

        @Test
        @DisplayName("OPEN 状态不可直接解决 - 抛出 BizException")
        void resolve_fromOpen_throwsBizException() {
            given(anomalyTraceMapper.selectById(1L)).willReturn(openAnomaly);

            assertThatThrownBy(() -> anomalyTraceService.resolve(1L, "原因", "处理人"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("INVESTIGATING");
        }

        @Test
        @DisplayName("RESOLVED 状态不可再次解决 - 抛出 BizException")
        void resolve_alreadyResolved_throwsBizException() {
            AnomalyTrace resolved = new AnomalyTrace();
            resolved.setId(5L);
            resolved.setStatus("RESOLVED");

            given(anomalyTraceMapper.selectById(5L)).willReturn(resolved);

            assertThatThrownBy(() -> anomalyTraceService.resolve(5L, "再次原因", "处理人"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("INVESTIGATING");
        }

        @Test
        @DisplayName("不存在的 ID - 抛出 BizException")
        void resolve_nonExistent_throwsBizException() {
            given(anomalyTraceMapper.selectById(999L)).willReturn(null);

            assertThatThrownBy(() -> anomalyTraceService.resolve(999L, "原因", "处理人"))
                    .isInstanceOf(BizException.class);
        }

        @Test
        @DisplayName("解决时设置 resolvedTime")
        void resolve_setsResolvedTime() {
            AnomalyTrace investigating = new AnomalyTrace();
            investigating.setId(6L);
            investigating.setStatus("INVESTIGATING");

            AnomalyTrace afterUpdate = new AnomalyTrace();
            afterUpdate.setId(6L);
            afterUpdate.setStatus("RESOLVED");
            afterUpdate.setResolvedTime(LocalDateTime.now());

            given(anomalyTraceMapper.selectById(6L))
                    .willReturn(investigating)
                    .willReturn(afterUpdate);
            given(anomalyTraceMapper.updateById(any(AnomalyTrace.class))).willReturn(1);

            AnomalyTrace result = anomalyTraceService.resolve(6L, "原因", "处理人");

            assertThat(result.getResolvedTime()).isNotNull();
        }
    }

    // ====================================================================== //
    //  impactAnalysis
    // ====================================================================== //

    @Nested
    @DisplayName("impactAnalysis 影响分析")
    class ImpactAnalysis {

        @Test
        @DisplayName("正常数据 - 返回 affectedBatches 和 downstreamOrders")
        void normalData_returnsAffectedBatchesAndOrders() {
            java.util.Map<String, Object> chainRow = new java.util.HashMap<>();
            chainRow.put("batch_no", "BATCH20260401001");
            chainRow.put("orchard_id", 100L);

            given(jdbcTemplate.queryForMap(anyString(), any()))
                    .willReturn(chainRow);
            given(jdbcTemplate.queryForObject(anyString(), any(Class.class), any()))
                    .willReturn(15)  // totalCodesInBatch
                    .willReturn(3);  // affectedTradeOrders

            Map<String, Object> result = anomalyTraceService.impactAnalysis("TC20260401001");

            assertThat(result).containsKey("traceCode");
            assertThat(result.get("traceCode")).isEqualTo("TC20260401001");
            assertThat(result.get("totalCodesInBatch")).isEqualTo(15);
            assertThat(result.get("affectedTradeOrders")).isEqualTo(3);
        }

        @Test
        @DisplayName("查询失败时返回 error 字段，不抛异常")
        void queryFails_returnsErrorField() {
            given(jdbcTemplate.queryForMap(anyString(), any()))
                    .willThrow(new RuntimeException("模拟数据库查询失败"));

            Map<String, Object> result = anomalyTraceService.impactAnalysis("INVALID_CODE");

            assertThat(result).containsKey("error");
            assertThat((String) result.get("error")).contains("部分数据查询失败");
        }
    }
}
