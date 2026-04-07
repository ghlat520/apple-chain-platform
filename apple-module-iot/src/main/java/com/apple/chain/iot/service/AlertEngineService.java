package com.apple.chain.iot.service;

import com.apple.chain.iot.entity.IotAlert;
import com.apple.chain.iot.entity.IotTelemetry;

import java.util.List;

/**
 * M3 alert rule engine.
 *
 * <p>Pure-function {@link #evaluate} so it can be unit tested without
 * touching the database. The service wrapper persists results.
 */
public interface AlertEngineService {

    /**
     * Apply the 4 baseline rules to a single telemetry sample.
     *
     * <ol>
     *   <li>TEMP_HIGH: any of temp_1..4 above 4°C → WARN</li>
     *   <li>TEMP_LOW:  any of temp_1..4 below 0°C → WARN</li>
     *   <li>DOOR_ANOMALY: door_status=1 (open) at high speed → WARN</li>
     *   <li>SENSOR_FAULT: max-min of temp_1..4 &gt; 1°C → INFO</li>
     * </ol>
     *
     * @return zero or more alerts (NOT yet persisted)
     */
    List<IotAlert> evaluate(IotTelemetry telemetry);

    /** Evaluate + persist. */
    List<IotAlert> evaluateAndPersist(IotTelemetry telemetry);

    /** Find devices that have not reported in {@code maxStaleMinutes} and emit OFFLINE alerts. */
    int sweepOfflineDevices(int maxStaleMinutes);
}
