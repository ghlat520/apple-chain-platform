package com.apple.chain.planting.service.impl;

import cn.hutool.core.util.IdUtil;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.HarvestBatch;
import com.apple.chain.planting.mapper.HarvestBatchMapper;
import com.apple.chain.planting.service.HarvestBatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HarvestBatchServiceImpl extends ServiceImpl<HarvestBatchMapper, HarvestBatch> implements HarvestBatchService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<HarvestBatch> listBatches(int page, int size, Long orchardId, String status) {
        LambdaQueryWrapper<HarvestBatch> wrapper = new LambdaQueryWrapper<HarvestBatch>()
                .eq(orchardId != null, HarvestBatch::getOrchardId, orchardId)
                .eq(status != null, HarvestBatch::getStatus, status)
                .orderByDesc(HarvestBatch::getHarvestDate);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public HarvestBatch getBatchDetail(Long id) {
        HarvestBatch batch = getById(id);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采收批次不存在");
        }
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HarvestBatch createBatch(HarvestBatch batch) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        batch.setBatchNo(String.format("HB%s%04d", prefix, seq));
        batch.setStatus("DRAFT");
        save(batch);
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HarvestBatch updateBatch(Long id, HarvestBatch batch) {
        HarvestBatch existing = getBatchDetail(id);
        if ("CONFIRMED".equals(existing.getStatus())) {
            throw new BizException("已确认的批次不能修改");
        }
        batch.setId(id);
        batch.setBatchNo(null);
        updateById(batch);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HarvestBatch confirmBatch(Long id) {
        HarvestBatch batch = getBatchDetail(id);
        if ("CONFIRMED".equals(batch.getStatus())) {
            throw new BizException("批次已经确认");
        }

        // Generate trace code using Hutool snowflake prefix + batch reference
        String traceCode = "TC" + batch.getBatchNo();

        HarvestBatch update = new HarvestBatch();
        update.setId(id);
        update.setStatus("CONFIRMED");
        update.setTraceCode(traceCode);
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long id) {
        HarvestBatch existing = getBatchDetail(id);
        if ("CONFIRMED".equals(existing.getStatus())) {
            throw new BizException("已确认批次不能删除");
        }
        removeById(id);
    }

    @Override
    public void exportBatches(Long orchardId, String status, HttpServletResponse response) {
        LambdaQueryWrapper<HarvestBatch> wrapper = new LambdaQueryWrapper<HarvestBatch>()
                .eq(orchardId != null, HarvestBatch::getOrchardId, orchardId)
                .eq(status != null, HarvestBatch::getStatus, status)
                .orderByDesc(HarvestBatch::getHarvestDate);
        List<HarvestBatch> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("采收批次.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("批次编号,果园ID,采收日期,总重量(kg),A级(kg),B级(kg),C级(kg),状态,溯源码");
            for (HarvestBatch b : list) {
                writer.println(
                        b.getBatchNo() + "," + b.getOrchardId() + "," +
                        b.getHarvestDate() + "," + b.getTotalWeight() + "," +
                        b.getGradeA() + "," + b.getGradeB() + "," + b.getGradeC() + "," +
                        b.getStatus() + "," + (b.getTraceCode() != null ? b.getTraceCode() : "")
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
