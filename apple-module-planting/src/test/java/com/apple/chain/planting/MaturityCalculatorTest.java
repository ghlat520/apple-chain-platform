package com.apple.chain.planting;

import com.apple.chain.planting.entity.MaturityRecord;
import com.apple.chain.planting.entity.MaturityStandard;
import com.apple.chain.planting.maturity.MaturityCalculator;
import com.apple.chain.planting.maturity.MaturityRecommendation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-function tests for {@link MaturityCalculator}.
 *
 * <p>Coverage matrix: 3 varieties × 3 ripeness stages = 9 baseline scenarios,
 * plus normalization edge cases.</p>
 */
@DisplayName("M6 — MaturityCalculator 单元测试")
class MaturityCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 1);

    // ─── variety baselines (mirror V13 seed data) ─────────────────────────────

    private static MaturityStandard redFuji() {
        MaturityStandard s = new MaturityStandard();
        s.setVariety("red_fuji");
        s.setBrixMin(new BigDecimal("13.50"));
        s.setBrixMax(new BigDecimal("16.00"));
        s.setFirmnessMin(new BigDecimal("6.50"));
        s.setFirmnessMax(new BigDecimal("8.50"));
        s.setColorTarget("C8281E");
        s.setAccumulateTempTarget(3200);
        s.setDailyTempIncrement(new BigDecimal("18.50"));
        return s;
    }

    private static MaturityStandard gala() {
        MaturityStandard s = new MaturityStandard();
        s.setVariety("gala");
        s.setBrixMin(new BigDecimal("11.00"));
        s.setBrixMax(new BigDecimal("13.50"));
        s.setFirmnessMin(new BigDecimal("5.80"));
        s.setFirmnessMax(new BigDecimal("7.80"));
        s.setColorTarget("D94A38");
        s.setAccumulateTempTarget(2400);
        s.setDailyTempIncrement(new BigDecimal("17.00"));
        return s;
    }

    private static MaturityStandard goldenDelicious() {
        MaturityStandard s = new MaturityStandard();
        s.setVariety("golden_delicious");
        s.setBrixMin(new BigDecimal("12.00"));
        s.setBrixMax(new BigDecimal("14.50"));
        s.setFirmnessMin(new BigDecimal("5.50"));
        s.setFirmnessMax(new BigDecimal("7.50"));
        s.setColorTarget("E8D547");
        s.setAccumulateTempTarget(2800);
        s.setDailyTempIncrement(new BigDecimal("18.00"));
        return s;
    }

    private static MaturityRecord sample(BigDecimal brix, BigDecimal firmness, String color, int accTemp) {
        MaturityRecord r = new MaturityRecord();
        r.setOrchardId(1L);
        r.setVariety("any");
        r.setSampleDate(TODAY);
        r.setBrix(brix);
        r.setFirmness(firmness);
        r.setColorRgb(color);
        r.setAccumulateTemp(accTemp);
        return r;
    }

    // ─── 3 varieties × 3 stages = 9 scenarios ─────────────────────────────────

    @Nested
    @DisplayName("红富士场景")
    class RedFujiScenarios {

        @Test
        @DisplayName("未熟样本 → UNRIPE，给出未来窗口")
        void unripe() {
            MaturityRecord r = sample(new BigDecimal("9.00"), new BigDecimal("9.50"), "8B5C2E", 2200);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, redFuji(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_UNRIPE);
            assertThat(rec.getScore()).isLessThan(MaturityCalculator.OPTIMAL_LOWER);
            assertThat(rec.getDaysUntilOptimal()).isGreaterThan(0);
            assertThat(rec.getWindowStart()).isAfter(TODAY);
        }

        @Test
        @DisplayName("最佳样本 → OPTIMAL，窗口为 today..today+7（部分因子未顶格）")
        void optimal() {
            // brix=1.0, firmness=1.0, color=1.0, temp at 50% target = 0.5
            // → score = 0.4 + 0.3 + 0.2 + 0.05 = 0.95 → 95.00 (boundary of OPTIMAL)
            MaturityRecord r = sample(new BigDecimal("14.50"), new BigDecimal("7.50"), "C8281E", 1600);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, redFuji(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OPTIMAL);
            assertThat(rec.getScore()).isBetween(MaturityCalculator.OPTIMAL_LOWER, MaturityCalculator.OPTIMAL_UPPER);
            assertThat(rec.getWindowStart()).isEqualTo(TODAY);
            assertThat(rec.getWindowEnd()).isEqualTo(TODAY.plusDays(MaturityCalculator.OPTIMAL_WINDOW_DAYS));
        }

        @Test
        @DisplayName("过熟样本 → OVERRIPE，立即采收（所有因子顶格 → 100 分）")
        void overripe() {
            // Spec: score > 95 ⇒ over-ripe. A perfect sample peaks all 4 factors at 1.0
            // → score = 100 > 95 ⇒ harvest immediately.
            MaturityRecord r = sample(new BigDecimal("14.50"), new BigDecimal("7.50"), "C8281E", 3200);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, redFuji(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OVERRIPE);
            assertThat(rec.getScore()).isGreaterThan(MaturityCalculator.OPTIMAL_UPPER);
            assertThat(rec.getDaysUntilOptimal()).isZero();
        }
    }

    @Nested
    @DisplayName("嘎拉场景")
    class GalaScenarios {

        @Test
        @DisplayName("未熟样本")
        void unripe() {
            MaturityRecord r = sample(new BigDecimal("7.00"), new BigDecimal("9.00"), "5C8B2E", 1500);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, gala(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_UNRIPE);
        }

        @Test
        @DisplayName("最佳样本")
        void optimal() {
            // 50% accumulated temp → boundary OPTIMAL (score = 95)
            MaturityRecord r = sample(new BigDecimal("12.00"), new BigDecimal("6.80"), "D94A38", 1200);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, gala(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OPTIMAL);
        }

        @Test
        @DisplayName("过熟样本")
        void overripe() {
            MaturityRecord r = sample(new BigDecimal("12.50"), new BigDecimal("6.20"), "D94A38", 3300);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, gala(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OVERRIPE);
        }
    }

    @Nested
    @DisplayName("黄元帅场景")
    class GoldenDeliciousScenarios {

        @Test
        @DisplayName("未熟样本")
        void unripe() {
            MaturityRecord r = sample(new BigDecimal("8.00"), new BigDecimal("9.20"), "8B7C2E", 1900);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, goldenDelicious(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_UNRIPE);
        }

        @Test
        @DisplayName("最佳样本")
        void optimal() {
            // 50% accumulated temp → boundary OPTIMAL (score = 95)
            MaturityRecord r = sample(new BigDecimal("13.00"), new BigDecimal("6.50"), "E8D547", 1400);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, goldenDelicious(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OPTIMAL);
        }

        @Test
        @DisplayName("过熟样本")
        void overripe() {
            MaturityRecord r = sample(new BigDecimal("14.00"), new BigDecimal("6.00"), "E8D547", 3700);
            MaturityRecommendation rec = MaturityCalculator.recommend(r, goldenDelicious(), TODAY);
            assertThat(rec.getStatus()).isEqualTo(MaturityCalculator.STATUS_OVERRIPE);
        }
    }

    // ─── normalization edge cases ─────────────────────────────────────────────

    @Test
    @DisplayName("normalizeRange — 范围内返回 1")
    void normalizeRangeInRange() {
        BigDecimal v = MaturityCalculator.normalizeRange(new BigDecimal("14"), new BigDecimal("13.5"), new BigDecimal("16"));
        assertThat(v).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("normalizeRange — 远低于下限返回低分")
    void normalizeRangeBelow() {
        BigDecimal v = MaturityCalculator.normalizeRange(new BigDecimal("3"), new BigDecimal("13.5"), new BigDecimal("16"));
        assertThat(v).isLessThan(new BigDecimal("0.3"));
    }

    @Test
    @DisplayName("colorMatch — 完全相同返回 1")
    void colorMatchExact() {
        BigDecimal v = MaturityCalculator.colorMatch("C8281E", "C8281E");
        assertThat(v).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("colorMatch — 互补色返回低分")
    void colorMatchOpposite() {
        BigDecimal v = MaturityCalculator.colorMatch("000000", "FFFFFF");
        assertThat(v).isLessThan(new BigDecimal("0.05"));
    }

    @Test
    @DisplayName("colorMatch — 非法 hex 返回 0")
    void colorMatchInvalid() {
        BigDecimal v = MaturityCalculator.colorMatch("XYZ", "C8281E");
        assertThat(v).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("predictDaysUntilOptimal — 已达目标返回 0")
    void predictReached() {
        MaturityRecord r = sample(new BigDecimal("14"), new BigDecimal("7"), "C8281E", 3200);
        int days = MaturityCalculator.predictDaysUntilOptimal(r, redFuji());
        assertThat(days).isZero();
    }

    @Test
    @DisplayName("predictDaysUntilOptimal — 未达目标按 daily increment 估算")
    void predictPending() {
        MaturityRecord r = sample(new BigDecimal("9"), new BigDecimal("9"), "8B5C2E", 2200);
        int days = MaturityCalculator.predictDaysUntilOptimal(r, redFuji());
        // gap = 1000, daily = 18.5 → 55 days
        assertThat(days).isBetween(50, 60);
    }

    @Test
    @DisplayName("confidence — 各因子高度一致时给出高置信度")
    void confidenceHigh() {
        MaturityRecord r = sample(new BigDecimal("14.50"), new BigDecimal("7.50"), "C8281E", 3200);
        BigDecimal c = MaturityCalculator.confidence(r, redFuji());
        assertThat(c).isGreaterThan(new BigDecimal("0.8"));
    }
}
