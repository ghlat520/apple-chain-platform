package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.mapper.TemperatureRecordMapper;
import com.apple.chain.coldchain.service.TemperatureRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemperatureRecordServiceImpl extends ServiceImpl<TemperatureRecordMapper, TemperatureRecord> implements TemperatureRecordService {

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
        save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        removeById(id);
    }
}
