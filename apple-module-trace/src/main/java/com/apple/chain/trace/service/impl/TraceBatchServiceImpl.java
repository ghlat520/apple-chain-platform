package com.apple.chain.trace.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trace.entity.TraceBatch;
import com.apple.chain.trace.entity.TraceRecord;
import com.apple.chain.trace.mapper.TraceBatchMapper;
import com.apple.chain.trace.mapper.TraceRecordMapper;
import com.apple.chain.trace.service.TraceBatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TraceBatch service implementation.
 * Handles batch lifecycle: CREATED → PROCESSING → COMPLETED → SHIPPED.
 * Computes SHA-256 blockchain hash on creation.
 */
@Service
@RequiredArgsConstructor
public class TraceBatchServiceImpl extends ServiceImpl<TraceBatchMapper, TraceBatch> implements TraceBatchService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TraceRecordMapper traceRecordMapper;

    @Override
    public IPage<TraceBatch> listBatches(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<TraceBatch> wrapper = new LambdaQueryWrapper<TraceBatch>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TraceBatch::getBatchCode, keyword)
                        .or().like(TraceBatch::getOrchardName, keyword)
                        .or().like(TraceBatch::getVariety, keyword))
                .eq(StringUtils.hasText(status), TraceBatch::getStatus, status)
                .orderByDesc(TraceBatch::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public TraceBatch getBatchDetail(Long id) {
        TraceBatch batch = getById(id);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "批次不存在");
        }
        return batch;
    }

    @Override
    public Map<String, Object> scanByBatchCode(String batchCode) {
        TraceBatch batch = baseMapper.findByBatchCode(batchCode);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "未找到溯源批次: " + batchCode);
        }
        List<TraceRecord> records = traceRecordMapper.findByBatchId(batch.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("batch", batch);
        result.put("records", records);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TraceBatch createBatch(TraceBatch batch) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        String batchCode = String.format("TB%s%04d", prefix, seq);
        batch.setBatchCode(batchCode);

        if (!StringUtils.hasText(batch.getStatus())) {
            batch.setStatus("CREATED");
        }

        // Compute SHA-256 blockchain hash
        batch.setBlockchainHash(computeHash(batch));
        save(batch);
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TraceBatch updateStatus(Long id, String status) {
        TraceBatch existing = getBatchDetail(id);
        validateStatusTransition(existing.getStatus(), status);

        TraceBatch update = new TraceBatch();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "批次不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TraceRecord addRecord(Long batchId, TraceRecord record) {
        TraceBatch batch = getBatchDetail(batchId);
        record.setBatchId(batchId);
        record.setBatchCode(batch.getBatchCode());
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalDateTime.now());
        }
        traceRecordMapper.insert(record);
        return record;
    }

    @Override
    public List<TraceRecord> listRecords(Long batchId) {
        getBatchDetail(batchId); // validate exists
        return traceRecordMapper.findByBatchId(batchId);
    }

    @Override
    public void exportBatches(String keyword, String status, HttpServletResponse response) {
        LambdaQueryWrapper<TraceBatch> wrapper = new LambdaQueryWrapper<TraceBatch>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TraceBatch::getBatchCode, keyword)
                        .or().like(TraceBatch::getOrchardName, keyword))
                .eq(StringUtils.hasText(status), TraceBatch::getStatus, status)
                .orderByDesc(TraceBatch::getCreateTime);
        List<TraceBatch> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("溯源批次列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("批次编码,果园ID,果园名称,采收日期,品种,等级,重量(kg),状态,区块链哈希,创建时间");
            for (TraceBatch b : list) {
                writer.println(
                        b.getBatchCode() + "," +
                        b.getOrchardId() + "," +
                        q(b.getOrchardName()) + "," +
                        b.getHarvestDate() + "," +
                        q(b.getVariety()) + "," +
                        b.getGrade() + "," +
                        b.getWeight() + "," +
                        b.getStatus() + "," +
                        b.getBlockchainHash() + "," +
                        b.getCreateTime()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    // ---- private helpers ----

    private String computeHash(TraceBatch batch) {
        String raw = batch.getBatchCode()
                + batch.getOrchardId()
                + batch.getHarvestDate()
                + batch.getVariety()
                + batch.getWeight();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new BizException("哈希计算失败: " + e.getMessage());
        }
    }

    private void validateStatusTransition(String current, String next) {
        boolean valid = switch (current) {
            case "CREATED" -> "PROCESSING".equals(next);
            case "PROCESSING" -> "COMPLETED".equals(next);
            case "COMPLETED" -> "SHIPPED".equals(next);
            default -> false;
        };
        if (!valid) {
            throw new BizException("状态流转不合法: " + current + " → " + next);
        }
    }

    private String q(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
