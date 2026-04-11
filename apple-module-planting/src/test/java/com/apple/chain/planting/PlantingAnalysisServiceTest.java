package com.apple.chain.planting;

import com.apple.chain.planting.dto.PlantingAnalysisVO;
import com.apple.chain.planting.entity.CultivationBatch;
import com.apple.chain.planting.entity.CultivationOperation;
import com.apple.chain.planting.entity.HarvestBatch;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.mapper.CultivationBatchMapper;
import com.apple.chain.planting.mapper.CultivationOperationMapper;
import com.apple.chain.planting.mapper.HarvestBatchMapper;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.service.impl.PlantingAnalysisServiceImpl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for PlantingAnalysisServiceImpl.
 * Verifies yield-per-mu, premium-rate, pest-incidence calculations, and plot comparison.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("种植分析服务单元测试")
class PlantingAnalysisServiceTest {

    @Mock
    private OrchardMapper orchardMapper;

    @Mock
    private CultivationBatchMapper cultivationBatchMapper;

    @Mock
    private HarvestBatchMapper harvestBatchMapper;

    @Mock
    private CultivationOperationMapper cultivationOperationMapper;

    @InjectMocks
    private PlantingAnalysisServiceImpl plantingAnalysisService;

    private Orchard orchard1;
    private Orchard orchard2;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // Initialize lambda cache for entities used in LambdaQueryWrapper
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        assistant.setCurrentNamespace("test");

        TableInfoHelper.initTableInfo(assistant, CultivationBatch.class);
        TableInfoHelper.initTableInfo(assistant, Orchard.class);
        TableInfoHelper.initTableInfo(assistant, HarvestBatch.class);
        TableInfoHelper.initTableInfo(assistant, CultivationOperation.class);
    }

    @BeforeEach
    void setUp() {
        orchard1 = new Orchard();
        orchard1.setId(100L);
        orchard1.setOrchardName("洛川果园A");
        orchard1.setAreaMu(new BigDecimal("50.0000"));
        orchard1.setVariety("红富士");

        orchard2 = new Orchard();
        orchard2.setId(200L);
        orchard2.setOrchardName("洛川果园B");
        orchard2.setAreaMu(new BigDecimal("30.0000"));
        orchard2.setVariety("嘎拉");
    }

    /** Helper: create CultivationBatch using setters (id is in BaseEntity, not in @Builder). */
    private CultivationBatch buildBatch(Long id, Long orchardId, String variety, Integer year, BigDecimal actualYield) {
        CultivationBatch b = CultivationBatch.builder()
                .orchardId(orchardId)
                .appleVariety(variety)
                .plantYear(year)
                .actualYield(actualYield)
                .build();
        b.setId(id);
        return b;
    }

    /** Helper: create CultivationOperation using setters (id is in BaseEntity, not in @Builder). */
    private CultivationOperation buildOp(Long id, Long batchId, String opType) {
        CultivationOperation op = CultivationOperation.builder()
                .batchId(batchId)
                .operationType(opType)
                .build();
        op.setId(id);
        return op;
    }

    // ====================================================================== //
    //  getYieldRanking
    // ====================================================================== //

    @Nested
    @DisplayName("getYieldRanking 亩产排名")
    class GetYieldRanking {

        @Test
        @DisplayName("正常数据 - 验证 yieldPerMu = totalYield / areaMu 计算")
        void normalData_yieldPerMuCalculatedCorrectly() {
            CultivationBatch batch1 = buildBatch(1L, 100L, "红富士", 2025, new BigDecimal("15000.00"));
            CultivationBatch batch2 = buildBatch(2L, 200L, "嘎拉", 2025, new BigDecimal("9000.00"));

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch1, batch2));
            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1, orchard2));

            List<PlantingAnalysisVO.YieldPerMu> result = plantingAnalysisService.getYieldRanking(null, 2025);

            assertThat(result).hasSize(2);

            // orchard1: 15000 / 50 = 300.0000
            PlantingAnalysisVO.YieldPerMu y1 = result.stream()
                    .filter(r -> r.getOrchardId().equals(100L)).findFirst().orElseThrow();
            assertThat(y1.getYieldPerMu()).isEqualByComparingTo("300.0000");
            assertThat(y1.getTotalYield()).isEqualByComparingTo("15000.00");
            assertThat(y1.getAreaMu()).isEqualByComparingTo("50.0000");

            // orchard2: 9000 / 30 = 300.0000
            PlantingAnalysisVO.YieldPerMu y2 = result.stream()
                    .filter(r -> r.getOrchardId().equals(200L)).findFirst().orElseThrow();
            assertThat(y2.getYieldPerMu()).isEqualByComparingTo("300.0000");
        }

        @Test
        @DisplayName("空数据 - 返回空列表，无 NPE")
        void emptyData_returnsEmptyList() {
            given(cultivationBatchMapper.selectList(any())).willReturn(Collections.emptyList());

            List<PlantingAnalysisVO.YieldPerMu> result = plantingAnalysisService.getYieldRanking(null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("areaMu 为零时 yieldPerMu 返回 0，不抛异常")
        void zeroAreaMu_yieldPerMuIsZero() {
            Orchard zeroAreaOrchard = new Orchard();
            zeroAreaOrchard.setId(300L);
            zeroAreaOrchard.setOrchardName("零面积果园");
            zeroAreaOrchard.setAreaMu(BigDecimal.ZERO);

            CultivationBatch batch = buildBatch(3L, 300L, "红富士", 2025, new BigDecimal("5000.00"));

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch));
            given(orchardMapper.selectList(any())).willReturn(List.of(zeroAreaOrchard));

            List<PlantingAnalysisVO.YieldPerMu> result = plantingAnalysisService.getYieldRanking(null, 2025);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getYieldPerMu()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("areaMu 为 null 时 yieldPerMu 返回 0")
        void nullAreaMu_yieldPerMuIsZero() {
            Orchard nullAreaOrchard = new Orchard();
            nullAreaOrchard.setId(400L);
            nullAreaOrchard.setOrchardName("无面积果园");
            nullAreaOrchard.setAreaMu(null);

            CultivationBatch batch = buildBatch(4L, 400L, "红富士", 2025, new BigDecimal("8000.00"));

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch));
            given(orchardMapper.selectList(any())).willReturn(List.of(nullAreaOrchard));

            List<PlantingAnalysisVO.YieldPerMu> result = plantingAnalysisService.getYieldRanking(null, 2025);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getYieldPerMu()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("按品种筛选 - 仅返回匹配品种的果园")
        void varietyFilter_returnsOnlyMatchingVariety() {
            CultivationBatch batch = buildBatch(5L, 100L, "红富士", 2025, new BigDecimal("15000.00"));

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch));
            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1));

            List<PlantingAnalysisVO.YieldPerMu> result = plantingAnalysisService.getYieldRanking("红富士", 2025);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVariety()).isEqualTo("红富士");
        }

        @Test
        @DisplayName("结果按 yieldPerMu 降序排列")
        void resultSortedByYieldPerMuDescending() {
            CultivationBatch batchHigh = buildBatch(10L, 100L, "红富士", 2025, new BigDecimal("15000.00"));
            CultivationBatch batchLow = buildBatch(11L, 200L, "嘎拉", 2025, new BigDecimal("3000.00"));

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batchHigh, batchLow));
            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1, orchard2));

            List<PlantingAnalysisVO.YieldPerMu> result = plantingAnalysisService.getYieldRanking(null, 2025);

            // orchard1: 300, orchard2: 100
            assertThat(result.get(0).getYieldPerMu())
                    .isGreaterThanOrEqualTo(result.get(1).getYieldPerMu());
        }
    }

    // ====================================================================== //
    //  getPremiumRates
    // ====================================================================== //

    @Nested
    @DisplayName("getPremiumRates 优果率")
    class GetPremiumRates {

        @Test
        @DisplayName("正常数据 - 验证 premiumRate = gradeAWeight / totalWeight 计算")
        void normalData_premiumRateCalculatedCorrectly() {
            HarvestBatch harvest1 = new HarvestBatch();
            harvest1.setId(1L);
            harvest1.setOrchardId(100L);
            harvest1.setTotalWeight(new BigDecimal("10000.00"));
            harvest1.setGradeA(new BigDecimal("8000.00"));

            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1));
            given(harvestBatchMapper.selectList(any())).willReturn(List.of(harvest1));

            List<PlantingAnalysisVO.PremiumRate> result = plantingAnalysisService.getPremiumRates("红富士", null);

            assertThat(result).hasSize(1);
            // 8000 / 10000 = 0.8000
            assertThat(result.get(0).getPremiumRate()).isEqualByComparingTo("0.8000");
            assertThat(result.get(0).getGradeAWeight()).isEqualByComparingTo("8000.00");
            assertThat(result.get(0).getTotalWeight()).isEqualByComparingTo("10000.00");
        }

        @Test
        @DisplayName("空数据 - 返回空列表")
        void emptyData_returnsEmptyList() {
            given(harvestBatchMapper.selectList(any())).willReturn(Collections.emptyList());

            List<PlantingAnalysisVO.PremiumRate> result = plantingAnalysisService.getPremiumRates(null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("品种筛选无匹配果园 - 返回空列表")
        void varietyFilterNoMatch_returnsEmptyList() {
            given(orchardMapper.selectList(any())).willReturn(Collections.emptyList());

            List<PlantingAnalysisVO.PremiumRate> result = plantingAnalysisService.getPremiumRates("不存在的品种", null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("totalWeight 为零时 premiumRate 返回 0")
        void zeroTotalWeight_premiumRateIsZero() {
            HarvestBatch harvest = new HarvestBatch();
            harvest.setId(2L);
            harvest.setOrchardId(100L);
            harvest.setTotalWeight(BigDecimal.ZERO);
            harvest.setGradeA(BigDecimal.ZERO);

            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1));
            given(harvestBatchMapper.selectList(any())).willReturn(List.of(harvest));

            List<PlantingAnalysisVO.PremiumRate> result = plantingAnalysisService.getPremiumRates(null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPremiumRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("结果按 premiumRate 降序排列")
        void resultSortedByPremiumRateDescending() {
            HarvestBatch h1 = new HarvestBatch();
            h1.setId(10L); h1.setOrchardId(100L);
            h1.setTotalWeight(new BigDecimal("10000")); h1.setGradeA(new BigDecimal("9000"));

            HarvestBatch h2 = new HarvestBatch();
            h2.setId(11L); h2.setOrchardId(200L);
            h2.setTotalWeight(new BigDecimal("10000")); h2.setGradeA(new BigDecimal("3000"));

            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1, orchard2));
            given(harvestBatchMapper.selectList(any())).willReturn(List.of(h1, h2));

            List<PlantingAnalysisVO.PremiumRate> result = plantingAnalysisService.getPremiumRates(null, null);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPremiumRate())
                    .isGreaterThanOrEqualTo(result.get(1).getPremiumRate());
        }
    }

    // ====================================================================== //
    //  getPestIncidences
    // ====================================================================== //

    @Nested
    @DisplayName("getPestIncidences 病虫害发生率")
    class GetPestIncidences {

        @Test
        @DisplayName("正常数据 - 验证 incidenceRate = pestOperations / totalOperations 计算")
        void normalData_incidenceRateCalculatedCorrectly() {
            CultivationBatch batch1 = buildBatch(1L, 100L, "红富士", 2025, null);

            CultivationOperation op1 = buildOp(1L, 1L, "PESTICIDE");
            CultivationOperation op2 = buildOp(2L, 1L, "FERTILIZE");
            CultivationOperation op3 = buildOp(3L, 1L, "PESTICIDE");
            CultivationOperation op4 = buildOp(4L, 1L, "PRUNE");

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch1));
            given(cultivationOperationMapper.selectList(any()))
                    .willReturn(List.of(op1, op2, op3, op4));
            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1));

            List<PlantingAnalysisVO.PestIncidence> result = plantingAnalysisService.getPestIncidences(2025);

            assertThat(result).hasSize(1);
            // 2 pesticide / 4 total = 0.5000
            assertThat(result.get(0).getIncidenceRate()).isEqualByComparingTo("0.5000");
            assertThat(result.get(0).getTotalOperations()).isEqualTo(4L);
            assertThat(result.get(0).getPestOperations()).isEqualTo(2L);
        }

        @Test
        @DisplayName("空数据 - 返回空列表")
        void emptyBatches_returnsEmptyList() {
            given(cultivationBatchMapper.selectList(any())).willReturn(Collections.emptyList());

            List<PlantingAnalysisVO.PestIncidence> result = plantingAnalysisService.getPestIncidences(2025);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("无 PESTICIDE 操作时 incidenceRate 为 0")
        void noPesticideOperations_incidenceRateIsZero() {
            CultivationBatch batch1 = buildBatch(1L, 100L, "红富士", 2025, null);

            CultivationOperation op1 = buildOp(1L, 1L, "FERTILIZE");
            CultivationOperation op2 = buildOp(2L, 1L, "PRUNE");

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch1));
            given(cultivationOperationMapper.selectList(any())).willReturn(List.of(op1, op2));
            given(orchardMapper.selectList(any())).willReturn(List.of(orchard1));

            List<PlantingAnalysisVO.PestIncidence> result = plantingAnalysisService.getPestIncidences(2025);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIncidenceRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.get(0).getPestOperations()).isEqualTo(0L);
        }

        @Test
        @DisplayName("无操作记录时返回空列表")
        void noOperations_returnsEmptyList() {
            CultivationBatch batch1 = buildBatch(1L, 100L, "红富士", 2025, null);

            given(cultivationBatchMapper.selectList(any())).willReturn(List.of(batch1));
            given(cultivationOperationMapper.selectList(any())).willReturn(Collections.emptyList());

            List<PlantingAnalysisVO.PestIncidence> result = plantingAnalysisService.getPestIncidences(2025);

            assertThat(result).isEmpty();
        }
    }

    // ====================================================================== //
    //  comparePlots
    // ====================================================================== //

    @Nested
    @DisplayName("comparePlots 地块对比")
    class ComparePlots {

        @Test
        @DisplayName("正常数据 - 每个果园包含全部 3 项指标")
        void normalData_containsAllThreeMetricsPerOrchard() {
            // --- yield data ---
            CultivationBatch batch1 = buildBatch(1L, 100L, "红富士", 2025, new BigDecimal("15000.00"));
            // pest: getPestIncidences(null) loads all batches -> needs batch with orchardId
            CultivationBatch batch2 = buildBatch(2L, 100L, "红富士", 2025, null);

            // cultivationBatchMapper.selectList called:
            //   1st: getYieldRanking -> batch1
            //   2nd: getPestIncidences(null) -> all batches for batchToOrchard map
            given(cultivationBatchMapper.selectList(any()))
                    .willReturn(List.of(batch1))
                    .willReturn(List.of(batch2));

            // orchardMapper.selectList called:
            //   1st: getYieldRanking fetchOrchardsById
            //   2nd: getPremiumRates fetchOrchardsById
            //   3rd: getPestIncidences fetchOrchardsById
            //   4th: comparePlots fetchOrchardsById
            given(orchardMapper.selectList(any()))
                    .willReturn(List.of(orchard1))
                    .willReturn(List.of(orchard1))
                    .willReturn(List.of(orchard1))
                    .willReturn(List.of(orchard1));

            // --- premium data ---
            HarvestBatch harvest = new HarvestBatch();
            harvest.setId(1L); harvest.setOrchardId(100L);
            harvest.setTotalWeight(new BigDecimal("10000.00"));
            harvest.setGradeA(new BigDecimal("8000.00"));
            given(harvestBatchMapper.selectList(any())).willReturn(List.of(harvest));

            // --- pest data: operations linked to batchId=2 -> orchardId=100 ---
            CultivationOperation op1 = buildOp(1L, 2L, "PESTICIDE");
            CultivationOperation op2 = buildOp(2L, 2L, "FERTILIZE");
            given(cultivationOperationMapper.selectList(any()))
                    .willReturn(List.of(op1, op2));

            List<PlantingAnalysisVO.PlotComparison> result = plantingAnalysisService.comparePlots(List.of(100L));

            assertThat(result).hasSize(1);
            PlantingAnalysisVO.PlotComparison comp = result.get(0);
            assertThat(comp.getOrchardId()).isEqualTo(100L);
            assertThat(comp.getOrchardName()).isEqualTo("洛川果园A");
            // All 3 metrics should be present
            assertThat(comp.getYieldMetrics()).isNotNull();
            assertThat(comp.getPremiumMetrics()).isNotNull();
            assertThat(comp.getPestMetrics()).isNotNull();
        }

        @Test
        @DisplayName("空 orchardIds 列表 - 返回空列表")
        void emptyOrchardIds_returnsEmptyList() {
            List<PlantingAnalysisVO.PlotComparison> result = plantingAnalysisService.comparePlots(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null orchardIds 列表 - 返回空列表")
        void nullOrchardIds_returnsEmptyList() {
            List<PlantingAnalysisVO.PlotComparison> result = plantingAnalysisService.comparePlots(null);

            assertThat(result).isEmpty();
        }
    }
}
