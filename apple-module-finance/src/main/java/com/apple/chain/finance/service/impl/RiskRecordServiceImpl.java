package com.apple.chain.finance.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.entity.RiskRecord;
import com.apple.chain.finance.mapper.RiskRecordMapper;
import com.apple.chain.finance.service.RiskRecordService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskRecordServiceImpl extends ServiceImpl<RiskRecordMapper, RiskRecord> implements RiskRecordService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<RiskRecord> listRecords(int page, int size, String keyword, String riskLevel, String status) {
        LambdaQueryWrapper<RiskRecord> wrapper = new LambdaQueryWrapper<RiskRecord>()
                .like(StringUtils.hasText(keyword), RiskRecord::getDescription, keyword)
                .eq(StringUtils.hasText(riskLevel), RiskRecord::getRiskLevel, riskLevel)
                .eq(StringUtils.hasText(status), RiskRecord::getStatus, status)
                .orderByDesc(RiskRecord::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public RiskRecord getRecordDetail(Long id) {
        RiskRecord record = getById(id);
        if (record == null) throw new BizException(ResultCode.NOT_FOUND, "风控记录不存在");
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskRecord createRecord(RiskRecord record) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        record.setRiskCode(String.format("RK%s%04d", prefix, seq));
        record.setStatus("OPEN");
        save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskRecord updateRecord(Long id, RiskRecord record) {
        getRecordDetail(id);
        record.setId(id);
        record.setRiskCode(null);
        updateById(record);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        getRecordDetail(id);
        removeById(id);
    }

    @Override
    public void exportRecords(String keyword, String riskLevel, String status, HttpServletResponse response) {
        List<RiskRecord> list = list(new LambdaQueryWrapper<RiskRecord>()
                .like(StringUtils.hasText(keyword), RiskRecord::getDescription, keyword)
                .eq(StringUtils.hasText(riskLevel), RiskRecord::getRiskLevel, riskLevel)
                .eq(StringUtils.hasText(status), RiskRecord::getStatus, status));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("风控记录.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("风控编号,关联类型,风险等级,风险类型,描述,处置措施,处理人,处理时间,状态");
            for (RiskRecord r : list) {
                writer.println(r.getRiskCode() + "," + r.getRelatedType() + "," + r.getRiskLevel() + "," +
                        r.getRiskType() + "," + r.getDescription() + "," + r.getMeasure() + "," +
                        r.getHandler() + "," + r.getHandleTime() + "," + r.getStatus());
            }
            writer.flush();
        } catch (Exception e) { throw new BizException("导出失败: " + e.getMessage()); }
    }
}
