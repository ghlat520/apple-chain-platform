package com.apple.domain.service;

import com.apple.common.exception.BusinessException;
import com.apple.domain.entity.QrCode;
import com.apple.domain.entity.TraceRecord;
import com.apple.domain.mapper.QrCodeMapper;
import com.apple.domain.mapper.TraceRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TraceService {

    private final TraceRecordMapper traceRecordMapper;
    private final QrCodeMapper qrCodeMapper;

    public Page<TraceRecord> listPage(int page, int size,
                                       String traceCode, Long batchId, String stage) {
        LambdaQueryWrapper<TraceRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(traceCode)) {
            wrapper.eq(TraceRecord::getTraceCode, traceCode);
        }
        if (batchId != null) {
            wrapper.eq(TraceRecord::getBatchId, batchId);
        }
        if (StringUtils.isNotBlank(stage)) {
            wrapper.eq(TraceRecord::getStage, stage);
        }
        wrapper.orderByDesc(TraceRecord::getOperationTime);
        return traceRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public TraceRecord getById(Long id) {
        TraceRecord record = traceRecordMapper.selectById(id);
        if (record == null) {
            throw BusinessException.of(404, "TraceRecord not found: " + id);
        }
        return record;
    }

    public TraceRecord create(TraceRecord record) {
        // Compute SHA-256 hash for tamper-evidence
        String raw = record.getTraceCode() + "|" + record.getStage() + "|"
                + record.getOperationTime() + "|" + record.getDescription();
        record.setHashValue(sha256Hex(raw));
        traceRecordMapper.insert(record);
        return record;
    }

    public List<TraceRecord> getChainByTraceCode(String traceCode) {
        return traceRecordMapper.selectList(
                new LambdaQueryWrapper<TraceRecord>()
                        .eq(TraceRecord::getTraceCode, traceCode)
                        .orderByAsc(TraceRecord::getOperationTime));
    }

    public List<TraceRecord> scanByTraceCode(String traceCode) {
        // Increment scan count
        QrCode qrCode = qrCodeMapper.selectOne(
                new LambdaQueryWrapper<QrCode>().eq(QrCode::getTraceCode, traceCode));
        if (qrCode != null) {
            qrCode.setScanCount(qrCode.getScanCount() == null ? 1 : qrCode.getScanCount() + 1);
            qrCodeMapper.updateById(qrCode);
        }
        return getChainByTraceCode(traceCode);
    }

    public long countTotal() {
        return traceRecordMapper.selectCount(null);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "hash-error";
        }
    }
}
