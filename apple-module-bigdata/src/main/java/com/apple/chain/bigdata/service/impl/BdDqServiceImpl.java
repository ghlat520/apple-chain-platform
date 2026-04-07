package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdDataAsset;
import com.apple.chain.bigdata.entity.BdDqCheckResult;
import com.apple.chain.bigdata.entity.BdDqRule;
import com.apple.chain.bigdata.mapper.BdDataAssetMapper;
import com.apple.chain.bigdata.mapper.BdDqCheckResultMapper;
import com.apple.chain.bigdata.mapper.BdDqRuleMapper;
import com.apple.chain.bigdata.service.BdDqService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data quality engine — real implementation (v2.0 P0).
 *
 * <p>Dispatches a {@link BdDqRule} to one of 6 SQL templates and runs it
 * against the underlying ClickHouse / MySQL data store via {@link JdbcTemplate}.
 * Resolves the physical table from {@link BdDataAsset#getTableName()}.</p>
 *
 * <p>Supported rule types (rule_type column):</p>
 * <ul>
 *   <li>NOT_NULL  — null fraction in {@code fieldName}</li>
 *   <li>UNIQUE    — duplicate fraction in {@code fieldName}</li>
 *   <li>RANGE     — values outside [min, max], expression JSON: {@code {"min":0,"max":100}}</li>
 *   <li>REGEX     — values not matching regex, expression: raw regex string</li>
 *   <li>ENUM      — values outside allowed set, expression JSON: {@code {"values":["A","B"]}}</li>
 *   <li>FRESHNESS — max(field) older than N hours, expression JSON: {@code {"hours":24}}</li>
 * </ul>
 *
 * <p>Score = (1 - badRows/totalRows) × 100. Pass logic depends on severity:
 * BLOCK requires badRows=0; ERROR/WARN/INFO require score ≥ pass threshold.</p>
 *
 * <p>All execution exceptions are caught and recorded on the result row
 * (errorMessage + pass=0); they are NEVER rethrown so the scheduler keeps
 * making progress on other rules.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BdDqServiceImpl
        extends ServiceImpl<BdDqRuleMapper, BdDqRule>
        implements BdDqService {

    private final BdDqCheckResultMapper resultMapper;
    private final BdDataAssetMapper dataAssetMapper;
    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${bigdata.dq.pass-threshold:95.00}")
    private BigDecimal passThreshold;

    @Override
    public BdDqCheckResult runCheck(Long ruleId) {
        BdDqRule rule = getById(ruleId);
        if (rule == null) {
            throw new IllegalArgumentException("dq rule not found: " + ruleId);
        }

        BdDqCheckResult result = new BdDqCheckResult();
        result.setRuleId(ruleId);
        result.setRuleCode(rule.getRuleCode());
        result.setCheckTime(LocalDateTime.now());

        try {
            String tableName = resolveTableName(rule);
            String fieldName = sanitizeIdent(rule.getFieldName());
            String ruleType = rule.getRuleType() == null
                    ? "" : rule.getRuleType().toUpperCase();

            long[] counts = dispatch(ruleType, tableName, fieldName, rule.getRuleExpr());
            long total = counts[0];
            long bad = counts[1];

            BigDecimal score = computeScore(total, bad);
            int pass = computePass(rule.getSeverity(), bad, score);

            result.setTotalRows(total);
            result.setBadRows(bad);
            result.setScore(score);
            result.setPass(pass);
        } catch (Exception e) {
            log.warn("[dq:exec-failed] ruleId={} ruleCode={} err={}",
                    ruleId, rule.getRuleCode(), e.getMessage());
            result.setTotalRows(0L);
            result.setBadRows(0L);
            result.setScore(BigDecimal.ZERO);
            result.setPass(0);
            result.setErrorMessage(truncate(e.getMessage(), 1000));
        }

        resultMapper.insert(result);

        rule.setLastRunTime(result.getCheckTime());
        rule.setLastScore(result.getScore());
        updateById(rule);
        return result;
    }

    @Override
    public IPage<BdDqCheckResult> listResults(Long ruleId, int page, int size) {
        LambdaQueryWrapper<BdDqCheckResult> wrapper = new LambdaQueryWrapper<>();
        if (ruleId != null) {
            wrapper.eq(BdDqCheckResult::getRuleId, ruleId);
        }
        wrapper.orderByDesc(BdDqCheckResult::getCheckTime);
        return resultMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ----- dispatching -----

    /**
     * @return long[2] — {totalRows, badRows}
     */
    private long[] dispatch(String ruleType, String table, String field, String expr) {
        switch (ruleType) {
            case "NOT_NULL":
                return runNotNull(table, field);
            case "UNIQUE":
                return runUnique(table, field);
            case "RANGE":
                return runRange(table, field, expr);
            case "REGEX":
                return runRegex(table, field, expr);
            case "ENUM":
                return runEnum(table, field, expr);
            case "FRESHNESS":
                return runFreshness(table, field, expr);
            default:
                throw new IllegalArgumentException("unsupported rule_type: " + ruleType);
        }
    }

    private long[] runNotNull(String table, String field) {
        String sql = "SELECT count(*) AS total, "
                + "sum(CASE WHEN " + field + " IS NULL THEN 1 ELSE 0 END) AS bad "
                + "FROM " + table;
        return queryCounts(sql);
    }

    private long[] runUnique(String table, String field) {
        String sql = "SELECT count(*) AS total, "
                + "(count(*) - count(DISTINCT " + field + ")) AS bad "
                + "FROM " + table;
        return queryCounts(sql);
    }

    private long[] runRange(String table, String field, String expr) {
        JsonNode node = parseExpr(expr);
        if (node == null || !node.has("min") || !node.has("max")) {
            throw new IllegalArgumentException("RANGE requires expr {\"min\":..,\"max\":..}");
        }
        BigDecimal min = node.get("min").decimalValue();
        BigDecimal max = node.get("max").decimalValue();
        String sql = "SELECT count(*) AS total, "
                + "sum(CASE WHEN " + field + " < ? OR " + field + " > ? THEN 1 ELSE 0 END) AS bad "
                + "FROM " + table;
        return queryCounts(sql, min, max);
    }

    private long[] runRegex(String table, String field, String expr) {
        if (expr == null || expr.isEmpty()) {
            throw new IllegalArgumentException("REGEX requires expr (regex string)");
        }
        // ClickHouse: match(col, pattern) returns 1 on match. Bad = NOT match.
        String sql = "SELECT count(*) AS total, "
                + "sum(CASE WHEN match(toString(" + field + "), ?) = 1 THEN 0 ELSE 1 END) AS bad "
                + "FROM " + table;
        return queryCounts(sql, expr);
    }

    private long[] runEnum(String table, String field, String expr) {
        JsonNode node = parseExpr(expr);
        if (node == null || !node.has("values") || !node.get("values").isArray()) {
            throw new IllegalArgumentException("ENUM requires expr {\"values\":[..]}");
        }
        List<Object> allowed = new ArrayList<>();
        Iterator<JsonNode> it = node.get("values").elements();
        while (it.hasNext()) {
            allowed.add(it.next().asText());
        }
        if (allowed.isEmpty()) {
            throw new IllegalArgumentException("ENUM values is empty");
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < allowed.size(); i++) {
            if (i > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
        }
        String sql = "SELECT count(*) AS total, "
                + "sum(CASE WHEN " + field + " NOT IN (" + placeholders + ") THEN 1 ELSE 0 END) AS bad "
                + "FROM " + table;
        return queryCounts(sql, allowed.toArray());
    }

    private long[] runFreshness(String table, String field, String expr) {
        JsonNode node = parseExpr(expr);
        if (node == null || !node.has("hours")) {
            throw new IllegalArgumentException("FRESHNESS requires expr {\"hours\":N}");
        }
        int hours = node.get("hours").asInt();
        // Total = 1 (the table itself), bad = 1 if max(field) is stale.
        String sql = "SELECT 1 AS total, "
                + "CASE WHEN max(" + field + ") IS NULL "
                + "      OR max(" + field + ") < (now() - INTERVAL " + hours + " HOUR) "
                + "     THEN 1 ELSE 0 END AS bad "
                + "FROM " + table;
        return queryCounts(sql);
    }

    private long[] queryCounts(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rn) -> new long[]{
                    rs.getLong("total"),
                    rs.getLong("bad")
            }, args);
        } catch (DataAccessException e) {
            throw new IllegalStateException("dq sql failed: " + sql + " — " + e.getMessage(), e);
        }
    }

    // ----- helpers -----

    private String resolveTableName(BdDqRule rule) {
        if (rule.getAssetId() != null) {
            BdDataAsset asset = dataAssetMapper.selectById(rule.getAssetId());
            if (asset != null && asset.getTableName() != null && !asset.getTableName().isEmpty()) {
                return sanitizeIdent(asset.getTableName());
            }
        }
        if (rule.getAssetCode() != null && !rule.getAssetCode().isEmpty()) {
            // Fallback: assetCode used as table name (legacy convention)
            return sanitizeIdent(rule.getAssetCode());
        }
        throw new IllegalArgumentException(
                "rule has no resolvable target table: ruleId=" + rule.getId());
    }

    /**
     * Allow only [A-Za-z0-9_.] in identifiers (table/column). Anything else
     * triggers immediate rejection — never concatenate raw user input into SQL.
     */
    private String sanitizeIdent(String ident) {
        if (ident == null || ident.isEmpty()) {
            throw new IllegalArgumentException("identifier is empty");
        }
        if (!ident.matches("[A-Za-z0-9_.]+")) {
            throw new IllegalArgumentException("illegal identifier: " + ident);
        }
        return ident;
    }

    private JsonNode parseExpr(String expr) {
        if (expr == null || expr.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readTree(expr);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid rule_expr JSON: " + expr, e);
        }
    }

    private BigDecimal computeScore(long total, long bad) {
        if (total <= 0) {
            return new BigDecimal("100.00");
        }
        BigDecimal goodRate = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(bad).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP));
        return goodRate.multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int computePass(String severity, long bad, BigDecimal score) {
        if ("BLOCK".equalsIgnoreCase(severity)) {
            return bad == 0 ? 1 : 0;
        }
        return score.compareTo(passThreshold) >= 0 ? 1 : 0;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }
}
