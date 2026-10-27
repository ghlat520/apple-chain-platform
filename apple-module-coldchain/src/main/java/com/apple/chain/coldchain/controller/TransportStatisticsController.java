package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.entity.Vehicle;
import com.apple.chain.coldchain.mapper.TemperatureRecordMapper;
import com.apple.chain.coldchain.mapper.TransportTaskMapper;
import com.apple.chain.coldchain.mapper.VehicleMapper;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "运输统计")
@RestController
@RequestMapping("/api/coldchain/statistics")
@RequiredArgsConstructor
public class TransportStatisticsController {

    private final TransportTaskMapper transportTaskMapper;
    private final VehicleMapper vehicleMapper;
    private final TemperatureRecordMapper temperatureRecordMapper;

    @Operation(summary = "统计概览")
    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> data = new HashMap<>();

        // task counts by status
        List<TransportTask> allTasks = transportTaskMapper.selectList(
                new LambdaQueryWrapper<TransportTask>().select(TransportTask::getStatus));
        Map<String, Long> taskByStatus = allTasks.stream()
                .collect(Collectors.groupingBy(TransportTask::getStatus, Collectors.counting()));
        data.put("totalTasks", (long) allTasks.size());
        data.put("taskByStatus", taskByStatus);

        // vehicle counts by status
        List<Vehicle> allVehicles = vehicleMapper.selectList(
                new LambdaQueryWrapper<Vehicle>().select(Vehicle::getStatus));
        Map<String, Long> vehicleByStatus = allVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getStatus, Collectors.counting()));
        data.put("totalVehicles", (long) allVehicles.size());
        data.put("vehicleByStatus", vehicleByStatus);

        // alarm count total
        long totalAlarms = temperatureRecordMapper.selectCount(
                new LambdaQueryWrapper<TemperatureRecord>().eq(TemperatureRecord::getIsAlarm, 1));
        data.put("totalAlarms", totalAlarms);

        return R.ok(data);
    }

    @Operation(summary = "近N天报警统计")
    @GetMapping("/alarms")
    public R<Map<String, Object>> alarms(@RequestParam(defaultValue = "30") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        List<TemperatureRecord> alarms = temperatureRecordMapper.selectList(
                new LambdaQueryWrapper<TemperatureRecord>()
                        .eq(TemperatureRecord::getIsAlarm, 1)
                        .ge(TemperatureRecord::getRecordTime, since)
                        .orderByDesc(TemperatureRecord::getRecordTime));

        Map<String, Long> alarmByTask = alarms.stream()
                .collect(Collectors.groupingBy(
                        r -> String.valueOf(r.getTaskId()), Collectors.counting()));

        Map<String, Object> data = new HashMap<>();
        data.put("days", days);
        data.put("since", since);
        data.put("totalAlarms", (long) alarms.size());
        data.put("alarmByTask", alarmByTask);
        data.put("records", alarms);
        return R.ok(data);
    }
}
