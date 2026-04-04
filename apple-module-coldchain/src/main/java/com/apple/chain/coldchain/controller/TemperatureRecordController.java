package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.service.TemperatureRecordService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "温度监控")
@RestController
@RequestMapping("/api/coldchain/temperatures")
@RequiredArgsConstructor
public class TemperatureRecordController {

    private final TemperatureRecordService temperatureRecordService;

    @Operation(summary = "温度记录列表（分页）")
    @GetMapping("/list")
    public R<PageResult<TemperatureRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Integer isAlarm) {
        return R.ok(PageResult.of(temperatureRecordService.listRecords(page, size, taskId, isAlarm)));
    }

    @Operation(summary = "按任务查询温度记录")
    @GetMapping("/by-task")
    public R<List<TemperatureRecord>> byTask(@RequestParam Long taskId) {
        return R.ok(temperatureRecordService.listByTask(taskId));
    }

    @Operation(summary = "新增温度记录")
    @PostMapping
    public R<TemperatureRecord> create(@RequestBody TemperatureRecord record) {
        return R.ok(temperatureRecordService.createRecord(record));
    }

    @Operation(summary = "删除温度记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        temperatureRecordService.deleteRecord(id);
        return R.ok("删除成功", null);
    }
}
