package com.apple.chain.iot.service;

import com.apple.chain.iot.dto.TelemetryDTO;
import com.apple.chain.iot.entity.IotTelemetry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * M3 telemetry ingest service. Idempotent on (device_sn, collect_time).
 */
public interface TelemetryIngestService {

    /** Ingest a batch from the edge gateway; runs alert rules per row. */
    int ingestBatch(List<TelemetryDTO> batch);

    /** Most recent N samples for a device. */
    List<IotTelemetry> recentSamples(String deviceSn, LocalDateTime since, int limit);

    /** Latest single sample. */
    IotTelemetry latest(String deviceSn);
}
