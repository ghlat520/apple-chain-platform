package com.apple.chain.bigdata;

import com.apple.chain.bigdata.entity.BdAnalysisReport;
import com.apple.chain.bigdata.mapper.BdAnalysisReportMapper;
import com.apple.chain.bigdata.service.impl.BdAnalysisReportServiceImpl;
import com.apple.chain.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;

/**
 * Unit tests for BdAnalysisReportServiceImpl.
 * Verifies report generation for WEEKLY/MONTHLY/SEASONAL types, and publish workflow.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("大数据分析报告服务单元测试")
class BdAnalysisReportServiceTest {

    @Mock
    private BdAnalysisReportMapper bdAnalysisReportMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private BdAnalysisReportServiceImpl bdAnalysisReportService;

    private static final String PERIOD = "2026-04";

    @BeforeEach
    void setUp() throws Exception {
        bdAnalysisReportService = new BdAnalysisReportServiceImpl(jdbcTemplate, objectMapper);
        // ServiceImpl.baseMapper is protected; inject mock via reflection
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(bdAnalysisReportService, bdAnalysisReportMapper);
    }

    // ====================================================================== //
    //  generate
    // ====================================================================== //

    @Nested
    @DisplayName("generate 生成报告")
    class Generate {

        @BeforeEach
        void setUpJdbcMocks() {
            // Stub all 4 aggregation queries to return sample data
            Map<String, Object> plantingRow = Map.of("batch_count", 12L, "total_harvest", 50000L);
            Map<String, Object> tradeRow = Map.of("order_count", 30L, "trade_amount", 150000L);
            Map<String, Object> warehouseRow = Map.of("wh_count", 5L, "total_cap", 200000L, "used_cap", 120000L);
            Map<String, Object> financeRow = Map.of("loan_count", 8L, "total_amt", 500000L);

            given(jdbcTemplate.queryForMap(anyString(), any()))
                    .willReturn(plantingRow)  // aggregatePlanting
                    .willReturn(tradeRow)     // aggregateTrade
                    .willReturn(warehouseRow);// aggregateFinance (aggregateWarehouse has no param variant)

            // aggregateWarehouse uses no-param queryForMap
            given(jdbcTemplate.queryForMap(anyString()))
                    .willReturn(warehouseRow);

            // aggregateTrade also calls queryForList for topVarieties
            given(jdbcTemplate.queryForList(anyString(), anyString()))
                    .willReturn(java.util.Collections.emptyList());
        }

        @Test
        @DisplayName("WEEKLY 类型 - 标题含 '周报'，报告编号以 W 开头")
        void generate_weeklyType_correctTitleAndReportNo() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            BdAnalysisReport result = bdAnalysisReportService.generate("WEEKLY", PERIOD);

            assertThat(result.getReportType()).isEqualTo("WEEKLY");
            assertThat(result.getTitle()).isEqualTo(PERIOD + " 周报");
            assertThat(result.getReportNo()).startsWith("RPT-W-");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getGeneratedTime()).isNotNull();
        }

        @Test
        @DisplayName("MONTHLY 类型 - 标题含 '月报'，报告编号以 M 开头")
        void generate_monthlyType_correctTitleAndReportNo() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            BdAnalysisReport result = bdAnalysisReportService.generate("MONTHLY", PERIOD);

            assertThat(result.getReportType()).isEqualTo("MONTHLY");
            assertThat(result.getTitle()).isEqualTo(PERIOD + " 月报");
            assertThat(result.getReportNo()).startsWith("RPT-M-");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("SEASONAL 类型 - 标题含 '季报'，报告编号以 S 开头")
        void generate_seasonalType_correctTitleAndReportNo() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            BdAnalysisReport result = bdAnalysisReportService.generate("SEASONAL", "2026-Q1");

            assertThat(result.getReportType()).isEqualTo("SEASONAL");
            assertThat(result.getTitle()).isEqualTo("2026-Q1 季报");
            assertThat(result.getReportNo()).startsWith("RPT-S-");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
        }

        @Test
        @DisplayName("未知类型 - 使用默认标题后缀")
        void generate_unknownType_defaultTitleSuffix() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            BdAnalysisReport result = bdAnalysisReportService.generate("UNKNOWN", PERIOD);

            assertThat(result.getTitle()).isEqualTo(PERIOD + " 分析报告");
            assertThat(result.getReportNo()).startsWith("RPT-R-");
        }

        @Test
        @DisplayName("自动计算 period 日期范围 - period 直接透传到 SQL 查询")
        void generate_periodPassedToAggregationQueries() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            ArgumentCaptor<String> periodCaptor = ArgumentCaptor.forClass(String.class);

            bdAnalysisReportService.generate("MONTHLY", "2026-03");

            // Verify JdbcTemplate was called with the period substring (yearMonth)
            then(jdbcTemplate).should(atLeastOnce()).queryForMap(anyString(), periodCaptor.capture());
            assertThat(periodCaptor.getAllValues()).contains("2026-03");
        }

        @Test
        @DisplayName("生成后包含 summary 字段")
        void generate_containsSummaryField() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            BdAnalysisReport result = bdAnalysisReportService.generate("MONTHLY", PERIOD);

            assertThat(result.getSummary()).isNotNull();
            assertThat(result.getSummary()).contains("MONTHLY");
            assertThat(result.getSummary()).contains(PERIOD);
        }

        @Test
        @DisplayName("生成后包含四大板块 JSON 数据")
        void generate_containsFourSections() {
            given(bdAnalysisReportMapper.insert(any(BdAnalysisReport.class))).willReturn(1);

            BdAnalysisReport result = bdAnalysisReportService.generate("MONTHLY", PERIOD);

            assertThat(result.getPlantingSection()).isNotNull();
            assertThat(result.getTradeSection()).isNotNull();
            assertThat(result.getWarehouseSection()).isNotNull();
            assertThat(result.getFinanceSection()).isNotNull();
        }
    }

    // ====================================================================== //
    //  publish
    // ====================================================================== //

    @Nested
    @DisplayName("publish 发布报告")
    class Publish {

        @Test
        @DisplayName("DRAFT -> PUBLISHED 转换成功")
        void publish_draftToPublished_success() {
            BdAnalysisReport draft = new BdAnalysisReport();
            draft.setId(1L);
            draft.setStatus("DRAFT");
            draft.setTitle("测试报告");

            given(bdAnalysisReportMapper.selectById(1L)).willReturn(draft);
            given(bdAnalysisReportMapper.updateById(any(BdAnalysisReport.class))).willReturn(1);

            bdAnalysisReportService.publish(1L);

            then(bdAnalysisReportMapper).should().updateById(any(BdAnalysisReport.class));
        }

        @Test
        @DisplayName("PUBLISHED 状态不可重复发布 - 抛出 BizException")
        void publish_alreadyPublished_throwsBizException() {
            BdAnalysisReport published = new BdAnalysisReport();
            published.setId(2L);
            published.setStatus("PUBLISHED");

            given(bdAnalysisReportMapper.selectById(2L)).willReturn(published);

            assertThatThrownBy(() -> bdAnalysisReportService.publish(2L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已发布");
        }

        @Test
        @DisplayName("不存在的报告 ID - 抛出 BizException")
        void publish_nonExistent_throwsBizException() {
            given(bdAnalysisReportMapper.selectById(999L)).willReturn(null);

            assertThatThrownBy(() -> bdAnalysisReportService.publish(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("报告不存在");
        }
    }

    // ====================================================================== //
    //  getDetail
    // ====================================================================== //

    @Nested
    @DisplayName("getDetail 查询报告详情")
    class GetDetail {

        @Test
        @DisplayName("存在时返回报告")
        void getDetail_existing_returnsReport() {
            BdAnalysisReport report = new BdAnalysisReport();
            report.setId(1L);
            report.setTitle("2026-04 月报");

            given(bdAnalysisReportMapper.selectById(1L)).willReturn(report);

            BdAnalysisReport result = bdAnalysisReportService.getDetail(1L);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("2026-04 月报");
        }

        @Test
        @DisplayName("不存在时抛出 BizException")
        void getDetail_nonExistent_throwsBizException() {
            given(bdAnalysisReportMapper.selectById(999L)).willReturn(null);

            assertThatThrownBy(() -> bdAnalysisReportService.getDetail(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("报告不存在");
        }
    }
}
