package com.apple.chain.planting.geo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link GeoJsonAreaCalculator}.
 * Validates parsing, centroid, and area-in-mu calculation against known shapes.
 */
@DisplayName("GeoJsonAreaCalculator 单元测试")
class GeoJsonAreaCalculatorTest {

    /**
     * 1° latitude ≈ 111 km. A 0.001° square at lat 36.0 (烟台) is approximately
     * 111m × ~90m = ~9990 m² ≈ 14.99 mu. We accept 5% tolerance per the M4
     * acceptance criterion.
     */
    @Test
    @DisplayName("矩形小地块面积与亩数（误差 <5%）")
    void parse_smallRectangle_returnsExpectedAreaInMu() {
        String geo = """
                {
                  "type": "Polygon",
                  "coordinates": [[
                    [121.4400, 36.0000],
                    [121.4410, 36.0000],
                    [121.4410, 36.0010],
                    [121.4400, 36.0010],
                    [121.4400, 36.0000]
                  ]]
                }
                """;

        GeoJsonAreaCalculator.GeoResult r = GeoJsonAreaCalculator.parse(geo);

        // Center should be roughly (121.4405, 36.0005)
        assertThat(r.centerLng().doubleValue()).isCloseTo(121.4405, withinPercentage(0.001));
        assertThat(r.centerLat().doubleValue()).isCloseTo(36.0005, withinPercentage(0.001));

        // Area in mu: ~14.99 (true value computed from 111 km/° lat × cos(36°) for lng)
        // Real area ≈ 0.001° lat × 0.001° lng × 111000² × cos(36°) ≈ 9988 m² ≈ 14.98 亩
        double expectedMu = (0.001 * 111000.0) * (0.001 * 111000.0 * Math.cos(Math.toRadians(36.0))) / 666.67;
        assertThat(r.areaMu().doubleValue()).isCloseTo(expectedMu, withinPercentage(5.0));
    }

    @Test
    @DisplayName("三角形地块面积")
    void parse_triangle_returnsHalfRectangleArea() {
        String geo = """
                {
                  "type": "Polygon",
                  "coordinates": [[
                    [121.4400, 36.0000],
                    [121.4410, 36.0000],
                    [121.4400, 36.0010],
                    [121.4400, 36.0000]
                  ]]
                }
                """;
        GeoJsonAreaCalculator.GeoResult tri = GeoJsonAreaCalculator.parse(geo);

        String rect = """
                {
                  "type": "Polygon",
                  "coordinates": [[
                    [121.4400, 36.0000],
                    [121.4410, 36.0000],
                    [121.4410, 36.0010],
                    [121.4400, 36.0010],
                    [121.4400, 36.0000]
                  ]]
                }
                """;
        GeoJsonAreaCalculator.GeoResult r = GeoJsonAreaCalculator.parse(rect);

        // Triangle should be half of rectangle (within 1% rounding tolerance)
        assertThat(tri.areaMu().doubleValue())
                .isCloseTo(r.areaMu().doubleValue() / 2.0, withinPercentage(2.0));
    }

    @Test
    @DisplayName("非闭合环也能识别（首尾不重复时使用全部点）")
    void parse_unClosedRing_stillComputes() {
        // 4 distinct vertices, no closing duplicate -> still 4 points (>= 4 required)
        String geo = """
                {
                  "type": "Polygon",
                  "coordinates": [[
                    [121.4400, 36.0000],
                    [121.4410, 36.0000],
                    [121.4410, 36.0010],
                    [121.4400, 36.0010]
                  ]]
                }
                """;
        GeoJsonAreaCalculator.GeoResult r = GeoJsonAreaCalculator.parse(geo);
        assertThat(r.areaMu()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("空字符串抛出 IllegalArgumentException")
    void parse_blank_throws() {
        assertThatThrownBy(() -> GeoJsonAreaCalculator.parse(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GeoJsonAreaCalculator.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("非 Polygon 类型拒绝")
    void parse_nonPolygon_throws() {
        String geo = """
                {"type": "Point", "coordinates": [121.44, 36.0]}
                """;
        assertThatThrownBy(() -> GeoJsonAreaCalculator.parse(geo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Polygon");
    }

    @Test
    @DisplayName("少于 4 个点的环拒绝")
    void parse_tooFewPoints_throws() {
        String geo = """
                {
                  "type": "Polygon",
                  "coordinates": [[
                    [121.44, 36.0],
                    [121.45, 36.0],
                    [121.44, 36.0]
                  ]]
                }
                """;
        assertThatThrownBy(() -> GeoJsonAreaCalculator.parse(geo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(">= 4");
    }

    @Test
    @DisplayName("非法 JSON 拒绝")
    void parse_invalidJson_throws() {
        assertThatThrownBy(() -> GeoJsonAreaCalculator.parse("{not json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid GeoJSON");
    }
}
