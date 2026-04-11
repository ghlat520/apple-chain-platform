package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdAnalysisReport;
import com.apple.chain.bigdata.mapper.BdAnalysisReportMapper;
import com.apple.chain.bigdata.service.BdAnalysisReportService;
import com.apple.chain.common.exception.BizException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analysis report service implementation.
 * Aggregates data from planting, trade, warehouse, and finance tables via JdbcTemplate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BdAnalysisReportServiceImpl
        extends ServiceImpl<BdAnalysisReportMapper, BdAnalysisReport>
        implements BdAnalysisReportService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BdAnalysisReport generate(String type, String period) {
        BdAnalysisReport report = new BdAnalysisReport();
        report.setReportType(type);
        report.setPeriod(period);
        report.setTitle(buildTitle(type, period));
        report.setGeneratedTime(LocalDateTime.now());
        report.setStatus("DRAFT");
        report.setReportNo(buildReportNo(type, period));

        report.setPlantingSection(toJson(aggregatePlanting(period)));
        report.setTradeSection(toJson(aggregateTrade(period)));
        report.setWarehouseSection(toJson(aggregateWarehouse()));
        report.setFinanceSection(toJson(aggregateFinance()));
        report.setSummary(buildSummary(type, period));

        save(report);
        return report;
    }

    @Override
    public IPage<BdAnalysisReport> list(int page, int size) {
        return page(new Page<>(page, size));
    }

    @Override
    public BdAnalysisReport getDetail(Long id) {
        BdAnalysisReport report = getById(id);
        if (report == null) {
            throw new BizException("报告不存在: " + id);
        }
        return report;
    }

    @Override
    @Transactional
    public void publish(Long id) {
        BdAnalysisReport report = getDetail(id);
        if ("PUBLISHED".equals(report.getStatus())) {
            throw new BizException("报告已发布");
        }
        report.setStatus("PUBLISHED");
        updateById(report);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Map<String, Object> aggregatePlanting(String period) {
        Map<String, Object> data = new HashMap<>();
        try {
            String yearMonth = period.substring(0, 7); // e.g. 2026-04
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) as batch_count, " +
                    "COALESCE(SUM(harvest_weight),0) as total_harvest " +
                    "FROM pt_harvest_batch WHERE deleted=0 AND DATE_FORMAT(harvest_date,'%Y-%m')=?",
                    yearMonth);
            data.put("batchCount", row.get("batch_count"));
            data.put("totalHarvest", row.get("total_harvest"));
        } catch (Exception e) {
            log.warn("aggregatePlanting failed: {}", e.getMessage());
            data.put("error", "数据暂不可用");
        }
        return data;
    }

    private Map<String, Object> aggregateTrade(String period) {
        Map<String, Object> data = new HashMap<>();
        try {
            String yearMonth = period.substring(0, 7);
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) as order_count, " +
                    "COALESCE(SUM(total_amount),0) as trade_amount " +
                    "FROM td_trade_order WHERE deleted=0 AND DATE_FORMAT(trade_date,'%Y-%m')=?",
                    yearMonth);
            data.put("orderCount", row.get("order_count"));
            data.put("tradeAmount", row.get("trade_amount"));

            List<Map<String, Object>> topVarieties = jdbcTemplate.queryForList(
                    "SELECT variety, COUNT(*) as cnt FROM td_trade_order " +
                    "WHERE deleted=0 AND DATE_FORMAT(trade_date,'%Y-%m')=? " +
                    "GROUP BY variety ORDER BY cnt DESC LIMIT 5",
                    yearMonth);
            data.put("topVarieties", topVarieties);
        } catch (Exception e) {
            log.warn("aggregateTrade failed: {}", e.getMessage());
            data.put("error", "数据暂不可用");
        }
        return data;
    }

    private Map<String, Object> aggregateWarehouse() {
        Map<String, Object> data = new HashMap<>();
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) as wh_count, " +
                    "COALESCE(SUM(capacity),0) as total_cap, " +
                    "COALESCE(SUM(used_capacity),0) as used_cap " +
                    "FROM wh_warehouse WHERE deleted=0");
            data.put("warehouseCount", row.get("wh_count"));
            data.put("totalCapacity", row.get("total_cap"));
            data.put("usedCapacity", row.get("used_cap"));
        } catch (Exception e) {
            log.warn("aggregateWarehouse failed: {}", e.getMessage());
            data.put("error", "数据暂不可用");
        }
        return data;
    }

    private Map<String, Object> aggregateFinance() {
        Map<String, Object> data = new HashMap<>();
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) as loan_count, " +
                    "COALESCE(SUM(apply_amount),0) as total_amt " +
                    "FROM fn_loan WHERE deleted=0");
            data.put("loanCount", row.get("loan_count"));
            data.put("totalAmount", row.get("total_amt"));
        } catch (Exception e) {
            log.warn("aggregateFinance failed: {}", e.getMessage());
            data.put("error", "数据暂不可用");
        }
        return data;
    }

    private String buildTitle(String type, String period) {
        return switch (type) {
            case "WEEKLY" -> period + " 周报";
            case "MONTHLY" -> period + " 月报";
            case "SEASONAL" -> period + " 季报";
            default -> period + " 分析报告";
        };
    }

    private String buildReportNo(String type, String period) {
        String prefix = switch (type) {
            case "WEEKLY" -> "W";
            case "MONTHLY" -> "M";
            case "SEASONAL" -> "S";
            default -> "R";
        };
        return "RPT-" + prefix + "-" + period + "-" +
                DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now());
    }

    private String buildSummary(String type, String period) {
        return String.format("本报告为%s类型，统计周期：%s。数据来源：种植、交易、仓储、金融四大模块实时聚合。", type, period);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
