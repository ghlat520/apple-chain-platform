package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.dto.LogisticsFullVO;
import com.apple.chain.coldchain.entity.Delivery;
import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.entity.Vehicle;
import com.apple.chain.coldchain.mapper.DeliveryMapper;
import com.apple.chain.coldchain.mapper.TemperatureRecordMapper;
import com.apple.chain.coldchain.mapper.TransportTaskMapper;
import com.apple.chain.coldchain.mapper.VehicleMapper;
import com.apple.chain.coldchain.service.LogisticsQueryService;
import com.apple.chain.coldchain.service.TransportTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogisticsQueryServiceImpl implements LogisticsQueryService {

    private final TransportTaskService transportTaskService;
    private final VehicleMapper vehicleMapper;
    private final TemperatureRecordMapper temperatureRecordMapper;
    private final DeliveryMapper deliveryMapper;

    @Override
    public LogisticsFullVO getFullLogistics(Long taskId) {
        TransportTask task = transportTaskService.getTaskDetail(taskId);

        Vehicle vehicle = null;
        if (task.getVehicleId() != null) {
            vehicle = vehicleMapper.selectById(task.getVehicleId());
        }

        List<TemperatureRecord> tempRecords = temperatureRecordMapper.selectList(
                new LambdaQueryWrapper<TemperatureRecord>()
                        .eq(TemperatureRecord::getTaskId, taskId)
                        .orderByAsc(TemperatureRecord::getRecordTime));

        long alarmCount = tempRecords.stream()
                .filter(r -> Integer.valueOf(1).equals(r.getIsAlarm()))
                .count();

        Delivery delivery = deliveryMapper.selectOne(
                new LambdaQueryWrapper<Delivery>()
                        .eq(Delivery::getTaskId, taskId)
                        .orderByDesc(Delivery::getCreateTime)
                        .last("LIMIT 1"));

        LogisticsFullVO vo = new LogisticsFullVO();
        vo.setTask(task);
        vo.setVehicle(vehicle);
        vo.setTempRecords(tempRecords);
        vo.setDelivery(delivery);
        vo.setAlarmCount(alarmCount);
        return vo;
    }
}
