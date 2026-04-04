package com.apple.domain.service;

import com.apple.common.exception.BusinessException;
import com.apple.domain.entity.FarmRecord;
import com.apple.domain.mapper.FarmRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FarmRecordService {

    private final FarmRecordMapper farmRecordMapper;

    public Page<FarmRecord> listPage(int page, int size, Long orchardId,
                                      String recordType, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<FarmRecord> wrapper = new LambdaQueryWrapper<>();
        if (orchardId != null) {
            wrapper.eq(FarmRecord::getOrchardId, orchardId);
        }
        if (StringUtils.isNotBlank(recordType)) {
            wrapper.eq(FarmRecord::getRecordType, recordType);
        }
        if (startDate != null) {
            wrapper.ge(FarmRecord::getOperationDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(FarmRecord::getOperationDate, endDate);
        }
        wrapper.orderByDesc(FarmRecord::getOperationDate);
        return farmRecordMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public FarmRecord getById(Long id) {
        FarmRecord record = farmRecordMapper.selectById(id);
        if (record == null) {
            throw BusinessException.of(404, "FarmRecord not found: " + id);
        }
        return record;
    }

    public FarmRecord create(FarmRecord record) {
        farmRecordMapper.insert(record);
        return record;
    }

    public FarmRecord update(Long id, FarmRecord update) {
        FarmRecord existing = getById(id);
        existing.setRecordType(update.getRecordType());
        existing.setOperationDate(update.getOperationDate());
        existing.setOperatorName(update.getOperatorName());
        existing.setDescription(update.getDescription());
        existing.setProductName(update.getProductName());
        existing.setProductAmount(update.getProductAmount());
        existing.setUnit(update.getUnit());
        existing.setImageUrls(update.getImageUrls());
        farmRecordMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        getById(id);
        farmRecordMapper.deleteById(id);
    }
}
