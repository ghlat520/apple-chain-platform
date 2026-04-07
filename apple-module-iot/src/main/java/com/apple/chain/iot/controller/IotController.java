package com.apple.chain.iot.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.R;
import com.apple.chain.iot.dto.TelemetryDTO;
import com.apple.chain.iot.entity.IotAlert;
import com.apple.chain.iot.entity.IotTelemetry;
import com.apple.chain.iot.mapper.IotAlertMapper;
import com.apple.chain.iot.service.TelemetryIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * M3 IoT endpoints.
 */
@Tag(name = "M3 IoT 物联网")
@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
public class IotController {

    private final TelemetryIngestService ingestService;
    private final IotAlertMapper iotAlertMapper;

    @Operation(summary = "边缘网关批量上报", description = "幂等：device_sn+collect_time 重复写入会被静默忽略")
    @PostMapping("/telemetry/batch")
    @RequirePerm("logistics:write")
    public R<Integer> ingest(@Valid @RequestBody List<TelemetryDTO> batch) {
        return R.ok(ingestService.ingestBatch(batch));
    }

    @Operation(summary = "查询设备最近遥测点")
    @GetMapping("/telemetry/{deviceSn}/latest")
    @RequirePerm("logistics:read")
    public R<IotTelemetry> latest(@PathVariable String deviceSn) {
        return R.ok(ingestService.latest(deviceSn));
    }

    @Operation(summary = "查询设备一段时间内的遥测序列")
    @GetMapping("/telemetry/{deviceSn}/series")
    @RequirePerm("logistics:read")
    public R<List<IotTelemetry>> series(
            @PathVariable String deviceSn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "1000") int limit) {
        return R.ok(ingestService.recentSamples(deviceSn, since, limit));
    }

    @Operation(summary = "未处理告警列表")
    @GetMapping("/alerts/open")
    @RequirePerm("logistics:read")
    public R<List<IotAlert>> openAlerts(@RequestParam(defaultValue = "100") int limit) {
        return R.ok(iotAlertMapper.findOpen(Math.min(Math.max(limit, 1), 500)));
    }
}
