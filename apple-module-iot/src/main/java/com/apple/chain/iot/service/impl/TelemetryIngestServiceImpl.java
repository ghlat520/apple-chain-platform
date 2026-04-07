package com.apple.chain.iot.service.impl;

import com.apple.chain.iot.dto.TelemetryDTO;
import com.apple.chain.iot.entity.IotDevice;
import com.apple.chain.iot.entity.IotTelemetry;
import com.apple.chain.iot.mapper.IotDeviceMapper;
import com.apple.chain.iot.mapper.IotTelemetryMapper;
import com.apple.chain.iot.service.AlertEngineService;
import com.apple.chain.iot.service.TelemetryIngestService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryIngestServiceImpl implements TelemetryIngestService {

    private static final int MAX_BATCH = 1000;

    private final IotTelemetryMapper iotTelemetryMapper;
    private final IotDeviceMapper iotDeviceMapper;
    private final AlertEngineService alertEngineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int ingestBatch(List<TelemetryDTO> batch) {
        if (batch == null || batch.isEmpty()) {
            return 0;
        }
        if (batch.size() > MAX_BATCH) {
            throw new IllegalArgumentException("batch size exceeds " + MAX_BATCH);
        }
        int inserted = 0;
        for (TelemetryDTO dto : batch) {
            IotTelemetry t = toEntity(dto);
            try {
                iotTelemetryMapper.insert(t);
                inserted++;
            } catch (DuplicateKeyException e) {
                // Idempotent: a replay from edge gateway hits the unique index;
                // skip silently.
                continue;
            }
            alertEngineService.evaluateAndPersist(t);
            touchDeviceLastSeen(dto.getDeviceSn(), dto.getCollectTime());
        }
        return inserted;
    }

    @Override
    public List<IotTelemetry> recentSamples(String deviceSn, LocalDateTime since, int limit) {
        if (deviceSn == null || since == null) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(limit, 1), 5000);
        return iotTelemetryMapper.findSince(deviceSn, since, safeLimit);
    }

    @Override
    public IotTelemetry latest(String deviceSn) {
        if (deviceSn == null) return null;
        return iotTelemetryMapper.findLatest(deviceSn);
    }

    private static IotTelemetry toEntity(TelemetryDTO dto) {
        IotTelemetry t = new IotTelemetry();
        t.setDeviceSn(dto.getDeviceSn());
        t.setCollectTime(dto.getCollectTime());
        t.setLatitude(dto.getLatitude());
        t.setLongitude(dto.getLongitude());
        t.setAltitude(dto.getAltitude());
        t.setSpeed(dto.getSpeed());
        t.setTemp1(dto.getTemp1());
        t.setTemp2(dto.getTemp2());
        t.setTemp3(dto.getTemp3());
        t.setTemp4(dto.getTemp4());
        t.setHumidity(dto.getHumidity());
        t.setDoorStatus(dto.getDoorStatus());
        t.setSignalType(dto.getSignalType());
        return t;
    }

    private void touchDeviceLastSeen(String deviceSn, LocalDateTime time) {
        IotDevice d = iotDeviceMapper.findBySn(deviceSn);
        if (d == null) return;
        iotDeviceMapper.update(null,
                new LambdaUpdateWrapper<IotDevice>()
                        .eq(IotDevice::getId, d.getId())
                        .set(IotDevice::getLastOnlineTime, time)
                        .set(IotDevice::getStatus, IotDevice.STATUS_ONLINE));
    }
}
