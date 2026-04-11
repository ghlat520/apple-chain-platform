package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.mapper.TemperatureRecordMapper;
import com.apple.chain.coldchain.mapper.TransportTaskMapper;
import com.apple.chain.coldchain.service.TemperatureRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemperatureRecordServiceImpl extends ServiceImpl<TemperatureRecordMapper, TemperatureRecord> implements TemperatureRecordService {

    private static final BigDecimal DEFAULT_TEMP_MIN = new BigDecimal("0");
    private static final BigDecimal DEFAULT_TEMP_MAX = new BigDecimal("4");
    private static final BigDecimal TOLERANCE = new BigDecimal("2");

    private final TransportTaskMapper transportTaskMapper;

    @Override
    public IPage<TemperatureRecord> listRecords(int page, int size, Long taskId, Integer isAlarm) {
        LambdaQueryWrapper<TemperatureRecord> wrapper = new LambdaQueryWrapper<TemperatureRecord>()
                .eq(taskId != null, TemperatureRecord::getTaskId, taskId)
                .eq(isAlarm != null, TemperatureRecord::getIsAlarm, isAlarm)
                .orderByDesc(TemperatureRecord::getRecordTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<TemperatureRecord> listByTask(Long taskId) {
        return list(new LambdaQueryWrapper<TemperatureRecord>()
                .eq(TemperatureRecord::getTaskId, taskId)
                .orderByAsc(TemperatureRecord::getRecordTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemperatureRecord createRecord(TemperatureRecord record) {
        if (record.getIsAlarm() == null) {
            record.setIsAlarm(0);
        }
        if (record.getTaskId() != null && record.getTemperature() != null) {
            TransportTask task = transportTaskMapper.selectById(record.getTaskId());
            BigDecimal temp = record.getTemperature();
            BigDecimal lower;
            BigDecimal upper;
            if (task != null && task.getRequiredTemp() != null) {
                lower = task.getRequiredTemp().subtract(TOLERANCE);
                upper = task.getRequiredTemp().add(TOLERANCE);
            } else {
                lower = DEFAULT_TEMP_MIN;
                upper = DEFAULT_TEMP_MAX;
            }
            if (temp.compareTo(lower) < 0 || temp.compareTo(upper) > 0) {
                record.setIsAlarm(1);
                record.setAlarmMsg(String.format("温度异常: %.1f℃，要求范围 [%.1f, %.1f]℃",
                        temp, lower, upper));
            }
        }
        save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        removeById(id);
    }
}
