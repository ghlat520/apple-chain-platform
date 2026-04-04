package com.apple.domain.service;

import com.apple.common.exception.BusinessException;
import com.apple.domain.entity.HarvestBatch;
import com.apple.domain.mapper.HarvestBatchMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HarvestBatchService {

    private final HarvestBatchMapper harvestBatchMapper;

    public Page<HarvestBatch> listPage(int page, int size,
                                        Long orchardId, String grade, String status) {
        LambdaQueryWrapper<HarvestBatch> wrapper = new LambdaQueryWrapper<>();
        if (orchardId != null) {
            wrapper.eq(HarvestBatch::getOrchardId, orchardId);
        }
        if (StringUtils.isNotBlank(grade)) {
            wrapper.eq(HarvestBatch::getGrade, grade);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(HarvestBatch::getStatus, status);
        }
        wrapper.orderByDesc(HarvestBatch::getHarvestDate);
        return harvestBatchMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public List<HarvestBatch> listAll() {
        return harvestBatchMapper.selectList(new LambdaQueryWrapper<HarvestBatch>()
                .orderByDesc(HarvestBatch::getCreatedAt));
    }

    public HarvestBatch getById(Long id) {
        HarvestBatch batch = harvestBatchMapper.selectById(id);
        if (batch == null) {
            throw BusinessException.of(404, "HarvestBatch not found: " + id);
        }
        return batch;
    }

    public HarvestBatch create(HarvestBatch batch) {
        batch.setBatchCode(generateBatchCode());
        if (batch.getStatus() == null) {
            batch.setStatus("pending");
        }
        harvestBatchMapper.insert(batch);
        return batch;
    }

    public HarvestBatch update(Long id, HarvestBatch update) {
        HarvestBatch existing = getById(id);
        existing.setHarvestDate(update.getHarvestDate());
        existing.setEstimatedWeight(update.getEstimatedWeight());
        existing.setActualWeight(update.getActualWeight());
        existing.setGrade(update.getGrade());
        existing.setDescription(update.getDescription());
        harvestBatchMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        getById(id);
        harvestBatchMapper.deleteById(id);
    }

    public HarvestBatch changeStatus(Long id, String newStatus) {
        HarvestBatch existing = getById(id);
        existing.setStatus(newStatus);
        harvestBatchMapper.updateById(existing);
        return existing;
    }

    private String generateBatchCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "HB" + date + String.format("%03d", (int) (Math.random() * 1000));
    }
}
