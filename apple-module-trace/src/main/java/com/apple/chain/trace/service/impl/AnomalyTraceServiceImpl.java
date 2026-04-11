package com.apple.chain.trace.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trace.entity.AnomalyTrace;
import com.apple.chain.trace.mapper.AnomalyTraceMapper;
import com.apple.chain.trace.service.AnomalyTraceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnomalyTraceServiceImpl extends ServiceImpl<AnomalyTraceMapper, AnomalyTrace> implements AnomalyTraceService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<AnomalyTrace> listAnomalies(int page, int size, String type, String status, String severity) {
        LambdaQueryWrapper<AnomalyTrace> wrapper = new LambdaQueryWrapper<AnomalyTrace>()
                .eq(StringUtils.hasText(type), AnomalyTrace::getAnomalyType, type)
                .eq(StringUtils.hasText(status), AnomalyTrace::getStatus, status)
                .eq(StringUtils.hasText(severity), AnomalyTrace::getSeverity, severity)
                .orderByDesc(AnomalyTrace::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public AnomalyTrace getAnomalyDetail(Long id) {
        AnomalyTrace anomaly = getById(id);
        if (anomaly == null) {
            throw new BizException(ResultCode.NOT_FOUND, "异常记录不存在");
        }
        return anomaly;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnomalyTrace reportAnomaly(AnomalyTrace anomaly) {
        anomaly.setStatus("OPEN");
        save(anomaly);
        return anomaly;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnomalyTrace investigate(Long id) {
        AnomalyTrace existing = getAnomalyDetail(id);
        if (!"OPEN".equals(existing.getStatus())) {
            throw new BizException("只有 OPEN 状态的异常可以开始调查");
        }
        AnomalyTrace update = new AnomalyTrace();
        update.setId(id);
        update.setStatus("INVESTIGATING");
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnomalyTrace resolve(Long id, String rootCause, String resolvedBy) {
        AnomalyTrace existing = getAnomalyDetail(id);
        if (!"INVESTIGATING".equals(existing.getStatus())) {
            throw new BizException("只有 INVESTIGATING 状态的异常可以解决");
        }
        AnomalyTrace update = new AnomalyTrace();
        update.setId(id);
        update.setStatus("RESOLVED");
        update.setRootCauseAnalysis(rootCause);
        update.setResolvedBy(resolvedBy);
        update.setResolvedTime(LocalDateTime.now());
        updateById(update);
        return getById(id);
    }

    @Override
    public Map<String, Object> impactAnalysis(String traceCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("traceCode", traceCode);

        try {
            // Find batch from trace code
            Map<String, Object> chain = jdbcTemplate.queryForMap(
                    "SELECT batch_no, orchard_id FROM tr_trace_chain WHERE trace_code = ? AND deleted = 0",
                    traceCode);
            String batchNo = (String) chain.get("batch_no");
            result.put("batchNo", batchNo);

            // Count all trace codes from same batch
            Integer totalCodes = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM trace_code WHERE batch_id = (SELECT id FROM trace_batch WHERE batch_code = ? AND deleted = 0 LIMIT 1) AND deleted = 0",
                    Integer.class, batchNo);
            result.put("totalCodesInBatch", totalCodes != null ? totalCodes : 0);

            // Count related trade orders
            Integer tradeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM td_trade_order WHERE batch_code = ? AND deleted = 0",
                    Integer.class, batchNo);
            result.put("affectedTradeOrders", tradeCount != null ? tradeCount : 0);
        } catch (Exception e) {
            result.put("error", "部分数据查询失败: " + e.getMessage());
        }

        return result;
    }
}
