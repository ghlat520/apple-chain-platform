package com.apple.chain.trade;

import com.apple.chain.trade.entity.PurchaseNeed;
import com.apple.chain.trade.entity.SupplyInfo;
import com.apple.chain.trade.match.MatchAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("M7 MatchAlgorithm 单元测试")
class MatchAlgorithmTest {

    private SupplyInfo supply(String variety, String quality, double price, double qty) {
        SupplyInfo s = new SupplyInfo();
        s.setVariety(variety);
        s.setQuality(quality);
        s.setPriceExpected(BigDecimal.valueOf(price));
        s.setQuantity(BigDecimal.valueOf(qty));
        return s;
    }

    private PurchaseNeed demand(String variety, String quality, double maxPrice, double qty) {
        PurchaseNeed d = new PurchaseNeed();
        d.setVariety(variety);
        d.setQuality(quality);
        d.setPriceMax(BigDecimal.valueOf(maxPrice));
        d.setQuantity(BigDecimal.valueOf(qty));
        return d;
    }

    @Test
    @DisplayName("品种不匹配 → 0 分")
    void variety_mismatch_isZero() {
        BigDecimal s = MatchAlgorithm.score(
                supply("红富士", "A", 5.0, 1000),
                demand("嘎啦", "A", 10.0, 1000));
        assertThat(s).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("完美匹配 → ≥95")
    void perfect_match_high_score() {
        BigDecimal s = MatchAlgorithm.score(
                supply("红富士", "A", 5.0, 1000),
                demand("红富士", "A", 6.0, 1000));
        assertThat(s.doubleValue()).isGreaterThanOrEqualTo(95.0);
    }

    @Test
    @DisplayName("品种匹配但价格远低于期望 → 中等分")
    void price_too_low() {
        BigDecimal full = MatchAlgorithm.score(
                supply("红富士", "A", 10.0, 1000),
                demand("红富士", "A", 5.0, 1000));   // demand 50% of supply expected
        BigDecimal good = MatchAlgorithm.score(
                supply("红富士", "A", 10.0, 1000),
                demand("红富士", "A", 11.0, 1000));
        assertThat(full).isLessThan(good);
    }

    @Test
    @DisplayName("供应数量不足 → quantity 维度降分")
    void supply_under_quantity() {
        BigDecimal full = MatchAlgorithm.score(
                supply("红富士", "A", 5.0, 500),
                demand("红富士", "A", 6.0, 1000));
        BigDecimal exact = MatchAlgorithm.score(
                supply("红富士", "A", 5.0, 1000),
                demand("红富士", "A", 6.0, 1000));
        assertThat(full).isLessThan(exact);
    }

    @Test
    @DisplayName("品质低于要求 → quality 维度降分或归零")
    void supply_lower_quality() {
        BigDecimal cMatch = MatchAlgorithm.score(
                supply("红富士", "C", 5.0, 1000),
                demand("红富士", "A", 6.0, 1000));
        BigDecimal aMatch = MatchAlgorithm.score(
                supply("红富士", "A", 5.0, 1000),
                demand("红富士", "A", 6.0, 1000));
        assertThat(cMatch).isLessThan(aMatch);
    }

    @Test
    @DisplayName("null 输入返回 0")
    void null_inputs() {
        assertThat(MatchAlgorithm.score(null, null)).isEqualByComparingTo("0.00");
        assertThat(MatchAlgorithm.score(supply("红富士", "A", 5, 1000), null)).isEqualByComparingTo("0.00");
        assertThat(MatchAlgorithm.score(null, demand("红富士", "A", 5, 1000))).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("3 条相近 demand top 排序正确")
    void top_demands_sorted() {
        SupplyInfo s = supply("红富士", "A", 5.0, 1000);
        BigDecimal d1 = MatchAlgorithm.score(s, demand("红富士", "A", 6.0, 1000)); // best: A grade, price ok
        BigDecimal d2 = MatchAlgorithm.score(s, demand("红富士", "B", 6.0, 1000)); // mid: lower quality demand still OK
        BigDecimal d3 = MatchAlgorithm.score(s, demand("红富士", "A", 4.0, 1000)); // worst: price too low
        // d1 and d2 score equally on quality (supply A satisfies any demand) but d3
        // gets penalized on price → strict ordering
        assertThat(d1).isGreaterThanOrEqualTo(d2);
        assertThat(d2).isGreaterThan(d3);
    }
}
