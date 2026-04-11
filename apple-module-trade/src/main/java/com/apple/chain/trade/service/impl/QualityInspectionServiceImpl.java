package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.QualityInspection;
import com.apple.chain.trade.mapper.QualityInspectionMapper;
import com.apple.chain.trade.service.QualityInspectionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QualityInspectionServiceImpl extends ServiceImpl<QualityInspectionMapper, QualityInspection> implements QualityInspectionService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<QualityInspection> listInspections(int page, int size, Long orderId, String status, String result) {
        LambdaQueryWrapper<QualityInspection> wrapper = new LambdaQueryWrapper<QualityInspection>()
                .eq(orderId != null, QualityInspection::getOrderId, orderId)
                .eq(status != null, QualityInspection::getStatus, status)
                .eq(result != null, QualityInspection::getResult, result)
                .orderByDesc(QualityInspection::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public QualityInspection getDetail(Long id) {
        QualityInspection qi = getById(id);
        if (qi == null) throw new BizException(ResultCode.NOT_FOUND, "质检记录不存在");
        return qi;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QualityInspection createInspection(QualityInspection inspection) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        inspection.setInspectionNo(String.format("QI%s%04d", prefix, seq));
        inspection.setStatus("PENDING");
        if (inspection.getInspectionDate() == null) {
            inspection.setInspectionDate(LocalDate.now());
        }
        save(inspection);
        return inspection;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QualityInspection inspect(Long id, QualityInspection data) {
        QualityInspection existing = getDetail(id);
        if (!"PENDING".equals(existing.getStatus())) {
            throw new BizException("只有待检状态可以录入检验结果");
        }
        QualityInspection update = new QualityInspection();
        update.setId(id);
        update.setBrixValue(data.getBrixValue());
        update.setFirmnessValue(data.getFirmnessValue());
        update.setColorScore(data.getColorScore());
        update.setDefectRate(data.getDefectRate());
        update.setInspectorName(data.getInspectorName());
        update.setReportUrl(data.getReportUrl());
        update.setStatus("INSPECTED");

        // Auto-compute result
        boolean pass = true;
        if (data.getBrixValue() != null && data.getBrixValue().compareTo(new BigDecimal("12")) < 0) pass = false;
        if (data.getFirmnessValue() != null && data.getFirmnessValue().compareTo(new BigDecimal("6")) < 0) pass = false;
        if (data.getColorScore() != null && data.getColorScore().compareTo(new BigDecimal("70")) < 0) pass = false;
        if (data.getDefectRate() != null && data.getDefectRate().compareTo(new BigDecimal("5")) > 0) pass = false;
        update.setResult(pass ? "PASS" : "FAIL");

        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QualityInspection accept(Long id) {
        QualityInspection existing = getDetail(id);
        if (!"INSPECTED".equals(existing.getStatus())) throw new BizException("只有已检状态可以验收");
        QualityInspection update = new QualityInspection();
        update.setId(id);
        update.setStatus("ACCEPTED");
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QualityInspection dispute(Long id, String reason) {
        QualityInspection existing = getDetail(id);
        if (!"INSPECTED".equals(existing.getStatus())) throw new BizException("只有已检状态可以发起争议");
        QualityInspection update = new QualityInspection();
        update.setId(id);
        update.setStatus("DISPUTED");
        update.setRemark(reason);
        updateById(update);
        return getById(id);
    }
}
