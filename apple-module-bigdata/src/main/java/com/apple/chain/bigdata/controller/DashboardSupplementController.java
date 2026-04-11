package com.apple.chain.bigdata.controller;

import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard supplement endpoints providing cross-module aggregations via JdbcTemplate.
 * All queries target production tables (wh_warehouse, fn_loan, td_trade_order, wh_warehouse_record).
 */
@Tag(name = "大屏补充统计")
@RestController
@RequestMapping("/api/bigdata/dashboard")
@RequiredArgsConstructor
public class DashboardSupplementController {

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "仓储总量统计（仓库数/总容量/已用容量）")
    @GetMapping("/warehouse-stock")
    public R<Map<String, Object>> warehouseStock() {
        String sql = "SELECT COUNT(*) as cnt, " +
                "COALESCE(SUM(capacity),0) as cap, " +
                "COALESCE(SUM(used_capacity),0) as used " +
                "FROM wh_warehouse WHERE deleted=0";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        Map<String, Object> result = new HashMap<>();
        result.put("warehouseCount", row.get("cnt"));
        result.put("totalCapacity", row.get("cap"));
        result.put("usedCapacity", row.get("used"));
        return R.ok(result);
    }

    @Operation(summary = "金融规模统计（贷款笔数/贷款总额）")
    @GetMapping("/finance-scale")
    public R<Map<String, Object>> financeScale() {
        String sql = "SELECT COUNT(*) as cnt, " +
                "COALESCE(SUM(apply_amount),0) as amt " +
                "FROM fn_loan WHERE deleted=0";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        Map<String, Object> result = new HashMap<>();
        result.put("loanCount", row.get("cnt"));
        result.put("totalAmount", row.get("amt"));
        return R.ok(result);
    }

    @Operation(summary = "品种价格趋势（按日均价）")
    @GetMapping("/price-trend")
    public R<List<Map<String, Object>>> priceTrend(
            @RequestParam(defaultValue = "红富士") String variety,
            @RequestParam(defaultValue = "30") int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        String sql = "SELECT trade_date, AVG(unit_price) as avg_price " +
                "FROM td_trade_order " +
                "WHERE variety=? AND trade_date>=? AND deleted=0 " +
                "GROUP BY trade_date ORDER BY trade_date";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, variety, fromDate);
        return R.ok(rows);
    }

    @Operation(summary = "损耗率统计（入库量/出库量）")
    @GetMapping("/loss-rate")
    public R<Map<String, Object>> lossRate() {
        String sql = "SELECT " +
                "SUM(CASE WHEN record_type='INBOUND' THEN quantity ELSE 0 END) as inb, " +
                "SUM(CASE WHEN record_type='OUTBOUND' THEN quantity ELSE 0 END) as outb " +
                "FROM wh_warehouse_record WHERE deleted=0";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql);
        Map<String, Object> result = new HashMap<>();
        result.put("inbound", row.get("inb"));
        result.put("outbound", row.get("outb"));
        Number inb = (Number) row.get("inb");
        Number outb = (Number) row.get("outb");
        double inbVal = inb != null ? inb.doubleValue() : 0;
        double outbVal = outb != null ? outb.doubleValue() : 0;
        double lossRate = inbVal > 0 ? (inbVal - outbVal) / inbVal * 100 : 0;
        result.put("lossRatePct", Math.round(lossRate * 100.0) / 100.0);
        return R.ok(result);
    }
}
