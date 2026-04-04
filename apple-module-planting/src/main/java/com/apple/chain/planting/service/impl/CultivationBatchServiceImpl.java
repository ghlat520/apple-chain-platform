package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.CultivationBatch;
import com.apple.chain.planting.mapper.CultivationBatchMapper;
import com.apple.chain.planting.service.CultivationBatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CultivationBatch service implementation.
 */
@Service
public class CultivationBatchServiceImpl extends ServiceImpl<CultivationBatchMapper, CultivationBatch>
        implements CultivationBatchService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<CultivationBatch> listBatches(int page, int size, String keyword, String status, Long orchardId) {
        LambdaQueryWrapper<CultivationBatch> wrapper = new LambdaQueryWrapper<CultivationBatch>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(CultivationBatch::getBatchCode, keyword)
                        .or().like(CultivationBatch::getOrchardName, keyword)
                        .or().like(CultivationBatch::getAppleVariety, keyword))
                .eq(StringUtils.hasText(status), CultivationBatch::getStatus, status)
                .eq(orchardId != null, CultivationBatch::getOrchardId, orchardId)
                .orderByDesc(CultivationBatch::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public CultivationBatch getBatchDetail(Long id) {
        CultivationBatch batch = getById(id);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "种植批次不存在");
        }
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CultivationBatch createBatch(CultivationBatch batch) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        batch.setBatchCode(String.format("CB%s%04d", prefix, seq));
        if (!StringUtils.hasText(batch.getStatus())) {
            batch.setStatus("PLANTING");
        }
        save(batch);
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CultivationBatch updateBatch(Long id, CultivationBatch batch) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "种植批次不存在");
        }
        batch.setId(id);
        batch.setBatchCode(null); // code is immutable
        updateById(batch);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "种植批次不存在");
        }
    }

    @Override
    public void exportBatches(String keyword, String status, Long orchardId, HttpServletResponse response) {
        LambdaQueryWrapper<CultivationBatch> wrapper = new LambdaQueryWrapper<CultivationBatch>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(CultivationBatch::getBatchCode, keyword)
                        .or().like(CultivationBatch::getOrchardName, keyword)
                        .or().like(CultivationBatch::getAppleVariety, keyword))
                .eq(StringUtils.hasText(status), CultivationBatch::getStatus, status)
                .eq(orchardId != null, CultivationBatch::getOrchardId, orchardId)
                .orderByDesc(CultivationBatch::getCreateTime);
        List<CultivationBatch> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("种植批次.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF'); // UTF-8 BOM for Excel compatibility
            writer.println("批次编码,果园ID,果园名称,苹果品种,种植年份,预计产量(kg),实际产量(kg),采收日期,状态,备注,创建时间");
            for (CultivationBatch b : list) {
                writer.println(
                        b.getBatchCode() + "," +
                        b.getOrchardId() + "," +
                        q(b.getOrchardName()) + "," +
                        q(b.getAppleVariety()) + "," +
                        b.getPlantYear() + "," +
                        b.getExpectedYield() + "," +
                        b.getActualYield() + "," +
                        b.getHarvestDate() + "," +
                        b.getStatus() + "," +
                        q(b.getRemark()) + "," +
                        b.getCreateTime()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    private String q(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
