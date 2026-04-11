package com.apple.chain.trace;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trace.dto.TraceFullChainVO;
import com.apple.chain.trace.entity.TraceChain;
import com.apple.chain.trace.entity.TraceNode;
import com.apple.chain.trace.mapper.TraceChainMapper;
import com.apple.chain.trace.mapper.TraceNodeMapper;
import com.apple.chain.trace.service.impl.TraceAggregationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for TraceAggregationServiceImpl cross-module queries.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("溯源聚合服务单元测试")
class TraceAggregationServiceTest {

    @Mock
    private TraceChainMapper traceChainMapper;

    @Mock
    private TraceNodeMapper traceNodeMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TraceAggregationServiceImpl traceAggregationService;

    private TraceChain sampleChain;
    private TraceNode node1;
    private TraceNode node2;

    @BeforeEach
    void setUp() {
        sampleChain = new TraceChain();
        sampleChain.setId(1L);
        sampleChain.setTraceCode("TCHB20250101001");
        sampleChain.setProductType("APPLE");
        sampleChain.setBatchNo("HB20250101001");
        sampleChain.setOrchardId(100L);
        sampleChain.setFarmerId(200L);
        sampleChain.setCurrentStatus("SOLD");
        sampleChain.setDataHash("abc123hash");
        sampleChain.setChainStatus(1);

        node1 = new TraceNode();
        node1.setId(1L);
        node1.setTraceCode("TCHB20250101001");
        node1.setNodeType("PLANT");
        node1.setNodeTime(LocalDateTime.of(2025, 1, 15, 8, 0));
        node1.setSummary("种植阶段");

        node2 = new TraceNode();
        node2.setId(2L);
        node2.setTraceCode("TCHB20250101001");
        node2.setNodeType("HARVEST");
        node2.setNodeTime(LocalDateTime.of(2025, 9, 20, 10, 0));
        node2.setSummary("采收阶段");
    }

    @Test
    @DisplayName("getFullChain - 正常溯源码返回完整链路信息")
    void getFullChain_existingTraceCode_returnsPopulatedVO() {
        given(traceChainMapper.findByTraceCode("TCHB20250101001")).willReturn(sampleChain);
        given(traceNodeMapper.findByTraceCode("TCHB20250101001")).willReturn(List.of(node1, node2));

        // Cross-module orchard query
        Map<String, Object> orchardRow = Map.of(
                "name", "洛川果园A",
                "variety", "红富士",
                "location", "陕西省延安市洛川县",
                "area_mu", 50
        );
        given(jdbcTemplate.queryForList(anyString(), anyLong()))
                .willReturn(List.of(orchardRow))   // orchard
                .willReturn(List.of(Map.of("name", "张果农", "phone", "13800001111")))  // farmer
                .willReturn(Collections.emptyList())  // input materials
                .willReturn(Collections.emptyList()); // warehouse records

        // Trade query (by batchNo)
        Map<String, Object> tradeRow = Map.of(
                "order_no", "TD20250920001",
                "variety", "红富士",
                "quantity", 5000,
                "unit_price", 8.50,
                "total_amount", 42500.00,
                "order_status", "COMPLETED",
                "trade_date", "2025-09-22"
        );
        given(jdbcTemplate.queryForList(anyString(), anyString()))
                .willReturn(List.of(tradeRow));

        TraceFullChainVO result = traceAggregationService.getFullChain("TCHB20250101001");

        assertThat(result).isNotNull();
        assertThat(result.getTraceCode()).isEqualTo("TCHB20250101001");
        assertThat(result.getBatchNo()).isEqualTo("HB20250101001");
        assertThat(result.getCurrentStatus()).isEqualTo("SOLD");
        assertThat(result.getChainTxHash()).isEqualTo("abc123hash");
        assertThat(result.getChainStatus()).isEqualTo(1);
        assertThat(result.getOrchardName()).isEqualTo("洛川果园A");
        assertThat(result.getVariety()).isEqualTo("红富士");
        assertThat(result.getRegion()).isEqualTo("陕西省延安市洛川县");
        assertThat(result.getFarmerName()).isEqualTo("张果农");

        // Timeline nodes
        assertThat(result.getTimeline()).hasSize(2);
        assertThat(result.getTimeline().get(0).getNodeType()).isEqualTo("PLANT");
        assertThat(result.getTimeline().get(1).getNodeType()).isEqualTo("HARVEST");

        // Trade info
        assertThat(result.getTradeInfo()).isNotNull();
    }

    @Test
    @DisplayName("getFullChain - 不存在的溯源码抛出 BizException")
    void getFullChain_nonExistentTraceCode_throwsBizException() {
        given(traceChainMapper.findByTraceCode("NOTEXIST")).willReturn(null);

        assertThatThrownBy(() -> traceAggregationService.getFullChain("NOTEXIST"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("溯源码不存在");
    }

    @Test
    @DisplayName("getFullChain - 无时间线节点时 timeline 为空列表")
    void getFullChain_noNodes_timelineIsEmpty() {
        given(traceChainMapper.findByTraceCode("TCHB20250101001")).willReturn(sampleChain);
        given(traceNodeMapper.findByTraceCode("TCHB20250101001")).willReturn(Collections.emptyList());

        // Cross-module queries return empty
        given(jdbcTemplate.queryForList(anyString(), anyLong()))
                .willReturn(Collections.emptyList());
        given(jdbcTemplate.queryForList(anyString(), anyString()))
                .willReturn(Collections.emptyList());

        TraceFullChainVO result = traceAggregationService.getFullChain("TCHB20250101001");

        assertThat(result).isNotNull();
        assertThat(result.getTimeline()).isEmpty();
    }

    @Test
    @DisplayName("getFullChain - 跨模块查询失败时不影响主流程")
    void getFullChain_crossModuleQueryFails_stillReturnsResult() {
        given(traceChainMapper.findByTraceCode("TCHB20250101001")).willReturn(sampleChain);
        given(traceNodeMapper.findByTraceCode("TCHB20250101001")).willReturn(List.of(node1));
        given(jdbcTemplate.queryForList(anyString(), anyLong()))
                .willThrow(new RuntimeException("模拟跨模块查询失败"));

        TraceFullChainVO result = traceAggregationService.getFullChain("TCHB20250101001");

        assertThat(result).isNotNull();
        assertThat(result.getTraceCode()).isEqualTo("TCHB20250101001");
        assertThat(result.getTimeline()).hasSize(1);
        // Cross-module fields remain null (not crash)
        assertThat(result.getOrchardName()).isNull();
    }
}
