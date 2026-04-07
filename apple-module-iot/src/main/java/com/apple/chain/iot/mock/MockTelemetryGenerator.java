package com.apple.chain.iot.mock;

import com.apple.chain.iot.dto.TelemetryDTO;
import com.apple.chain.iot.entity.IotDevice;
import com.apple.chain.iot.mapper.IotDeviceMapper;
import com.apple.chain.iot.service.TelemetryIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * M3 mock telemetry source. Every 30s emits one synthetic sample per online
 * cold-truck device. Generates positions around a fixed Yantai center
 * (37.4638°N, 121.4405°E) with bounded random walk; temperatures stay in
 * [0,4]°C with 5% chance of an out-of-band spike to exercise the alert engine.
 *
 * <p>Activated only when {@code iot.mock.enabled=true} (off by default so
 * production deployments don't accidentally generate fake data).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "iot.mock.enabled", havingValue = "true")
public class MockTelemetryGenerator {

    /** Yantai default center (modern apple supply hub). */
    private static final double CENTER_LAT = 37.4638;
    private static final double CENTER_LNG = 121.4405;
    private static final double DRIFT_AMPLITUDE = 0.01;

    private static final double MIN_TEMP = 0.5;
    private static final double MAX_TEMP = 3.5;
    private static final double SPIKE_PROBABILITY = 0.05;

    private final Random random = new Random();
    private final AtomicLong tickCounter = new AtomicLong();

    private final IotDeviceMapper iotDeviceMapper;
    private final TelemetryIngestService ingestService;

    @Scheduled(fixedRateString = "${iot.mock.interval-ms:30000}")
    public void emitTick() {
        long tick = tickCounter.incrementAndGet();
        List<IotDevice> devices = iotDeviceMapper.findOnlineByType(IotDevice.TYPE_COLD_TRUCK);
        if (devices.isEmpty()) {
            log.debug("Mock IoT tick {}: no online cold-truck devices", tick);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<TelemetryDTO> batch = devices.stream()
                .map(d -> sampleFor(d.getDeviceSn(), now))
                .toList();
        try {
            int n = ingestService.ingestBatch(batch);
            log.debug("Mock IoT tick {}: ingested {} samples", tick, n);
        } catch (Exception e) {
            log.warn("Mock IoT tick {} failed: {}", tick, e.toString());
        }
    }

    private TelemetryDTO sampleFor(String sn, LocalDateTime now) {
        TelemetryDTO d = new TelemetryDTO();
        d.setDeviceSn(sn);
        d.setCollectTime(now);
        d.setLatitude(round(CENTER_LAT + (random.nextDouble() - 0.5) * DRIFT_AMPLITUDE, 7));
        d.setLongitude(round(CENTER_LNG + (random.nextDouble() - 0.5) * DRIFT_AMPLITUDE, 7));
        d.setAltitude(round(20 + random.nextDouble() * 50, 2));
        d.setSpeed(round(40 + random.nextDouble() * 20, 2));

        // 5% chance to inject a spike to exercise alerts
        boolean spike = random.nextDouble() < SPIKE_PROBABILITY;
        d.setTemp1(temp(spike));
        d.setTemp2(temp(false));
        d.setTemp3(temp(false));
        d.setTemp4(temp(false));
        d.setHumidity(round(85 + random.nextDouble() * 10, 2));
        d.setDoorStatus(random.nextDouble() < 0.02 ? 1 : 0);
        d.setSignalType("5G");
        return d;
    }

    private BigDecimal temp(boolean spike) {
        double v = MIN_TEMP + random.nextDouble() * (MAX_TEMP - MIN_TEMP);
        if (spike) {
            v = MAX_TEMP + 2.0 + random.nextDouble() * 3.0; // 5.5–10.5°C → triggers TEMP_HIGH
        }
        return round(v, 2);
    }

    private static BigDecimal round(double v, int scale) {
        return new BigDecimal(v).setScale(scale, RoundingMode.HALF_UP);
    }
}
