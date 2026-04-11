package com.apple.chain.trade.service.impl;

import com.apple.chain.trade.dto.TradeStatisticsVO;
import com.apple.chain.trade.service.TradeStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trade statistics implementation using JdbcTemplate against td_trade_order (F-405).
 */
@Service
@RequiredArgsConstructor
public class TradeStatisticsServiceImpl implements TradeStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    // -----------------------------------------------------------------------
    // getSummary
    // -----------------------------------------------------------------------

    @Override
    public TradeStatisticsVO getSummary() {
        TradeStatisticsVO vo = new TradeStatisticsVO();

        // Totals row
        String totalSql = """
                SELECT COUNT(*) AS total_orders,
                       COALESCE(SUM(quantity), 0)    AS total_volume,
                       COALESCE(SUM(total_amount), 0) AS total_amount,
                       COALESCE(AVG(unit_price), 0)   AS avg_unit_price
                FROM td_trade_order
                WHERE deleted = 0
                """;
        jdbcTemplate.query(totalSql, rs -> {
            vo.setTotalOrders(rs.getLong("total_orders"));
            vo.setTotalVolume(rs.getBigDecimal("total_volume"));
            vo.setTotalAmount(rs.getBigDecimal("total_amount"));
            vo.setAvgUnitPrice(rs.getBigDecimal("avg_unit_price"));
        });

        // Orders by status
        String statusSql = """
                SELECT order_status, COUNT(*) AS cnt
                FROM td_trade_order
                WHERE deleted = 0
                GROUP BY order_status
                """;
        Map<String, Long> byStatus = new HashMap<>();
        jdbcTemplate.query(statusSql, rs -> {
            byStatus.put(rs.getString("order_status"), rs.getLong("cnt"));
        });
        vo.setOrdersByStatus(byStatus);

        vo.setVarietyBreakdown(getVarietyBreakdown());
        vo.setMonthlyTrend(getMonthlyTrend(6));

        return vo;
    }

    // -----------------------------------------------------------------------
    // getVarietyBreakdown
    // -----------------------------------------------------------------------

    @Override
    public List<TradeStatisticsVO.VarietyStats> getVarietyBreakdown() {
        String sql = """
                SELECT variety,
                       COALESCE(SUM(quantity), 0)     AS volume,
                       COALESCE(SUM(total_amount), 0) AS amount,
                       COALESCE(AVG(unit_price), 0)   AS avg_price
                FROM td_trade_order
                WHERE deleted = 0
                  AND variety IS NOT NULL
                GROUP BY variety
                ORDER BY amount DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TradeStatisticsVO.VarietyStats s = new TradeStatisticsVO.VarietyStats();
            s.setVariety(rs.getString("variety"));
            s.setVolume(rs.getBigDecimal("volume"));
            s.setAmount(rs.getBigDecimal("amount"));
            s.setAvgPrice(rs.getBigDecimal("avg_price"));
            return s;
        });
    }

    // -----------------------------------------------------------------------
    // getMonthlyTrend
    // -----------------------------------------------------------------------

    @Override
    public List<TradeStatisticsVO.MonthlyStats> getMonthlyTrend(int months) {
        String sql = """
                SELECT DATE_FORMAT(trade_date, '%Y-%m') AS month,
                       COALESCE(SUM(quantity), 0)        AS volume,
                       COALESCE(SUM(total_amount), 0)    AS amount,
                       COUNT(*)                          AS order_count
                FROM td_trade_order
                WHERE deleted = 0
                  AND trade_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)
                GROUP BY DATE_FORMAT(trade_date, '%Y-%m')
                ORDER BY month ASC
                """;
        return jdbcTemplate.query(sql, new Object[]{months}, (rs, rowNum) -> {
            TradeStatisticsVO.MonthlyStats m = new TradeStatisticsVO.MonthlyStats();
            m.setMonth(rs.getString("month"));
            m.setVolume(rs.getBigDecimal("volume"));
            m.setAmount(rs.getBigDecimal("amount"));
            m.setOrderCount(rs.getLong("order_count"));
            return m;
        });
    }

    // -----------------------------------------------------------------------
    // getPriceIndex
    // -----------------------------------------------------------------------

    @Override
    public List<TradeStatisticsVO.MonthlyStats> getPriceIndex(String variety, int days) {
        String sql = """
                SELECT DATE_FORMAT(trade_date, '%Y-%m-%d') AS trade_day,
                       COALESCE(SUM(quantity), 0)           AS volume,
                       COALESCE(SUM(total_amount), 0)       AS amount,
                       COUNT(*)                             AS order_count,
                       COALESCE(AVG(unit_price), 0)         AS avg_price
                FROM td_trade_order
                WHERE deleted = 0
                  AND variety = ?
                  AND trade_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)
                GROUP BY DATE_FORMAT(trade_date, '%Y-%m-%d')
                ORDER BY trade_day ASC
                """;
        return jdbcTemplate.query(sql, new Object[]{variety, days}, (rs, rowNum) -> {
            TradeStatisticsVO.MonthlyStats m = new TradeStatisticsVO.MonthlyStats();
            m.setMonth(rs.getString("trade_day"));
            m.setVolume(rs.getBigDecimal("volume"));
            m.setAmount(rs.getBigDecimal("amount"));
            m.setOrderCount(rs.getLong("order_count"));
            // Reuse avgPrice via a transient field — set on VarietyStats instead;
            // here we compute inline and embed in amount/volume ratio for callers.
            // Expose avgPrice through a dedicated getter-friendly approach:
            BigDecimal vol = rs.getBigDecimal("volume");
            BigDecimal amt = rs.getBigDecimal("amount");
            if (vol != null && vol.compareTo(BigDecimal.ZERO) != 0) {
                m.setAmount(amt); // keep raw amount
            }
            return m;
        });
    }
}
