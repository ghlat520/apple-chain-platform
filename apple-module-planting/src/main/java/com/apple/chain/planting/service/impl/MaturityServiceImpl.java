package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.MaturityRecord;
import com.apple.chain.planting.entity.MaturityStandard;
import com.apple.chain.planting.mapper.MaturityRecordMapper;
import com.apple.chain.planting.mapper.MaturityStandardMapper;
import com.apple.chain.planting.maturity.MaturityCalculator;
import com.apple.chain.planting.maturity.MaturityRecommendation;
import com.apple.chain.planting.service.MaturityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Default {@link MaturityService} implementation. See interface for contract.
 */
@Service
@RequiredArgsConstructor
public class MaturityServiceImpl implements MaturityService {

    private final MaturityStandardMapper standardMapper;
    private final MaturityRecordMapper recordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaturityRecord recordMeasurement(MaturityRecord record) {
        if (record == null || record.getVariety() == null || record.getOrchardId() == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "果园ID与品种为必填项");
        }
        if (record.getBrix() == null || record.getFirmness() == null
                || record.getColorRgb() == null || record.getAccumulateTemp() == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "糖度/硬度/色泽/积温四项为必填项");
        }

        // Physical range validation
        validateBrix(record.getBrix());
        validateFirmness(record.getFirmness());
        validateColorRgb(record.getColorRgb());
        if (record.getAccumulateTemp() < 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "积温不能为负数");
        }

        MaturityStandard standard = findStandard(record.getVariety());

        if (record.getSampleDate() == null) {
            record.setSampleDate(LocalDate.now());
        }

        MaturityRecommendation rec = MaturityCalculator.recommend(record, standard, LocalDate.now());
        record.setMaturityScore(rec.getScore());
        record.setRecommendation(rec.getStatus());

        recordMapper.insert(record);
        return record;
    }

    @Override
    public MaturityRecommendation recommendHarvestWindow(Long orchardId) {
        if (orchardId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "缺少 orchardId");
        }
        MaturityRecord latest = latestRecord(orchardId);
        if (latest == null) {
            throw new BizException(ResultCode.NOT_FOUND, "该果园暂无成熟度采样数据");
        }
        MaturityStandard standard = findStandard(latest.getVariety());
        return MaturityCalculator.recommend(latest, standard, LocalDate.now());
    }

    @Override
    public List<MaturityStandard> listStandards() {
        return standardMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public List<MaturityRecord> listOrchardRecords(Long orchardId, int limit) {
        if (orchardId == null) return List.of();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return recordMapper.selectList(new LambdaQueryWrapper<MaturityRecord>()
                .eq(MaturityRecord::getOrchardId, orchardId)
                .orderByDesc(MaturityRecord::getSampleDate)
                .orderByDesc(MaturityRecord::getId)
                .last("LIMIT " + safeLimit));
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private void validateBrix(BigDecimal brix) {
        if (brix.compareTo(BigDecimal.ZERO) <= 0 || brix.compareTo(new BigDecimal("30")) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "糖度值超出合理范围(0-30)");
        }
    }

    private void validateFirmness(BigDecimal firmness) {
        if (firmness.compareTo(BigDecimal.ZERO) <= 0 || firmness.compareTo(new BigDecimal("20")) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "硬度值超出合理范围(0-20)");
        }
    }

    private void validateColorRgb(String colorRgb) {
        if (!colorRgb.matches("^[0-9A-Fa-f]{6}$")) {
            throw new BizException(ResultCode.PARAM_ERROR, "色泽格式应为6位十六进制(如C8281E)");
        }
    }

    private MaturityStandard findStandard(String variety) {
        MaturityStandard standard = standardMapper.selectOne(
                new LambdaQueryWrapper<MaturityStandard>()
                        .eq(MaturityStandard::getVariety, variety)
                        .last("LIMIT 1"));
        if (standard == null) {
            throw new BizException(ResultCode.NOT_FOUND, "未找到品种成熟度标准: " + variety);
        }
        return standard;
    }

    private MaturityRecord latestRecord(Long orchardId) {
        List<MaturityRecord> rows = recordMapper.selectList(
                new LambdaQueryWrapper<MaturityRecord>()
                        .eq(MaturityRecord::getOrchardId, orchardId)
                        .orderByDesc(MaturityRecord::getSampleDate)
                        .orderByDesc(MaturityRecord::getId)
                        .last("LIMIT 1"));
        return rows.isEmpty() ? null : rows.get(0);
    }
}
