package com.apple.chain.planting;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.planting.entity.MaturityRecord;
import com.apple.chain.planting.entity.MaturityStandard;
import com.apple.chain.planting.mapper.MaturityRecordMapper;
import com.apple.chain.planting.mapper.MaturityStandardMapper;
import com.apple.chain.planting.maturity.MaturityCalculator;
import com.apple.chain.planting.maturity.MaturityRecommendation;
import com.apple.chain.planting.service.impl.MaturityServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("M6 — MaturityService 集成边界测试")
class MaturityServiceTest {

    @Mock
    private MaturityStandardMapper standardMapper;

    @Mock
    private MaturityRecordMapper recordMapper;

    @InjectMocks
    private MaturityServiceImpl service;

    private MaturityStandard redFuji;

    @BeforeEach
    void setUp() {
        redFuji = new MaturityStandard();
        redFuji.setId(9001L);
        redFuji.setVariety("red_fuji");
        redFuji.setBrixMin(new BigDecimal("13.50"));
        redFuji.setBrixMax(new BigDecimal("16.00"));
        redFuji.setFirmnessMin(new BigDecimal("6.50"));
        redFuji.setFirmnessMax(new BigDecimal("8.50"));
        redFuji.setColorTarget("C8281E");
        redFuji.setAccumulateTempTarget(3200);
        redFuji.setDailyTempIncrement(new BigDecimal("18.50"));
    }

    @Test
    @DisplayName("recordMeasurement — 成功保存并填充评分与推荐")
    void recordMeasurement_success() {
        given(standardMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(redFuji);
        given(recordMapper.insert(any(MaturityRecord.class))).willReturn(1);

        MaturityRecord r = new MaturityRecord();
        r.setOrchardId(20001L);
        r.setVariety("red_fuji");
        r.setBrix(new BigDecimal("14.50"));
        r.setFirmness(new BigDecimal("7.50"));
        r.setColorRgb("C8281E");
        r.setAccumulateTemp(1600); // 50% target → boundary OPTIMAL
        r.setOperator("张师傅");

        MaturityRecord saved = service.recordMeasurement(r);

        assertThat(saved.getMaturityScore()).isNotNull();
        assertThat(saved.getRecommendation()).isEqualTo(MaturityCalculator.STATUS_OPTIMAL);
        assertThat(saved.getSampleDate()).isEqualTo(LocalDate.now());
        then(recordMapper).should().insert(any(MaturityRecord.class));
    }

    @Test
    @DisplayName("recordMeasurement — 缺失字段抛 BizException")
    void recordMeasurement_missingFields() {
        MaturityRecord r = new MaturityRecord();
        r.setOrchardId(1L);
        r.setVariety("red_fuji");
        // brix etc missing
        assertThatThrownBy(() -> service.recordMeasurement(r))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("必填项");
    }

    @Test
    @DisplayName("recordMeasurement — 未知品种抛 BizException")
    void recordMeasurement_unknownVariety() {
        given(standardMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(null);

        MaturityRecord r = new MaturityRecord();
        r.setOrchardId(1L);
        r.setVariety("unknown");
        r.setBrix(new BigDecimal("14"));
        r.setFirmness(new BigDecimal("7"));
        r.setColorRgb("C8281E");
        r.setAccumulateTemp(3000);

        assertThatThrownBy(() -> service.recordMeasurement(r))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("未找到品种成熟度标准");
    }

    @Test
    @DisplayName("recommendHarvestWindow — 无样本时抛 BizException")
    void recommend_noSamples() {
        given(recordMapper.selectList(any(LambdaQueryWrapper.class))).willReturn(List.of());
        assertThatThrownBy(() -> service.recommendHarvestWindow(20001L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("暂无成熟度采样数据");
    }

    @Test
    @DisplayName("recommendHarvestWindow — 基于最新样本返回 OPTIMAL 推荐")
    void recommend_optimalSample() {
        MaturityRecord latest = new MaturityRecord();
        latest.setOrchardId(20001L);
        latest.setVariety("red_fuji");
        latest.setSampleDate(LocalDate.now());
        latest.setBrix(new BigDecimal("14.50"));
        latest.setFirmness(new BigDecimal("7.50"));
        latest.setColorRgb("C8281E");
        latest.setAccumulateTemp(1600); // 50% target → boundary OPTIMAL

        given(recordMapper.selectList(any(LambdaQueryWrapper.class))).willReturn(List.of(latest));
        given(standardMapper.selectOne(any(LambdaQueryWrapper.class))).willReturn(redFuji);

        MaturityRecommendation rec = service.recommendHarvestWindow(20001L);
        assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OPTIMAL);
        assertThat(rec.getWindowEnd()).isEqualTo(rec.getWindowStart().plusDays(7));
    }

    @Test
    @DisplayName("listStandards — 透传 mapper 结果")
    void listStandards() {
        given(standardMapper.selectList(any(LambdaQueryWrapper.class))).willReturn(List.of(redFuji));
        assertThat(service.listStandards()).hasSize(1);
    }
}
