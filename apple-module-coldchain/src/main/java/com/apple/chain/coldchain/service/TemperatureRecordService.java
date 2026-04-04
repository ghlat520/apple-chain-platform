package com.apple.chain.coldchain.service;

import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TemperatureRecordService extends IService<TemperatureRecord> {

    IPage<TemperatureRecord> listRecords(int page, int size, Long taskId, Integer isAlarm);

    List<TemperatureRecord> listByTask(Long taskId);

    TemperatureRecord createRecord(TemperatureRecord record);

    void deleteRecord(Long id);
}
