package com.apple.chain.iot.service.impl;

import com.apple.chain.iot.entity.IotAlert;
import com.apple.chain.iot.entity.IotDevice;
import com.apple.chain.iot.entity.IotTelemetry;
import com.apple.chain.iot.mapper.IotAlertMapper;
import com.apple.chain.iot.mapper.IotDeviceMapper;
import com.apple.chain.iot.service.AlertEngineService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEngineServiceImpl implements AlertEngineService {

    /** Cold-chain temperature window (°C). */
    public static final BigDecimal TEMP_MAX = new BigDecimal("4.0");
    public static final BigDecimal TEMP_MIN = new BigDecimal("0.0");

    /** Sensor disagreement threshold (°C). */
    public static final BigDecimal SENSOR_DEVIATION_LIMIT = new BigDecimal("1.0");

    /** Speed (km/h) above which an open door is considered anomalous. */
    public static final BigDecimal DOOR_OPEN_SPEED_THRESHOLD = new BigDecimal("5.0");

    private final IotAlertMapper iotAlertMapper;
    private final IotDeviceMapper iotDeviceMapper;

    @Override
    public List<IotAlert> evaluate(IotTelemetry t) {
        List<IotAlert> alerts = new ArrayList<>();
        if (t == null) return alerts;

        BigDecimal[] temps = {t.getTemp1(), t.getTemp2(), t.getTemp3(), t.getTemp4()};

        // Rule 1: TEMP_HIGH
        BigDecimal maxTemp = maxNotNull(temps);
        if (maxTemp != null && maxTemp.compareTo(TEMP_MAX) > 0) {
            alerts.add(buildAlert(t.getDeviceSn(), IotAlert.TYPE_TEMP_HIGH, IotAlert.LEVEL_WARN,
                    TEMP_MAX.toPlainString(), maxTemp.toPlainString(),
                    "温度过高: " + maxTemp + "°C"));
        }

        // Rule 2: TEMP_LOW
        BigDecimal minTemp = minNotNull(temps);
        if (minTemp != null && minTemp.compareTo(TEMP_MIN) < 0) {
            alerts.add(buildAlert(t.getDeviceSn(), IotAlert.TYPE_TEMP_LOW, IotAlert.LEVEL_WARN,
                    TEMP_MIN.toPlainString(), minTemp.toPlainString(),
                    "温度过低: " + minTemp + "°C"));
        }

        // Rule 3: DOOR_ANOMALY (open while moving)
        if (t.getDoorStatus() != null && t.getDoorStatus() == 1
                && t.getSpeed() != null && t.getSpeed().compareTo(DOOR_OPEN_SPEED_THRESHOLD) > 0) {
            alerts.add(buildAlert(t.getDeviceSn(), IotAlert.TYPE_DOOR_ANOMALY, IotAlert.LEVEL_WARN,
                    "speed<" + DOOR_OPEN_SPEED_THRESHOLD,
                    "speed=" + t.getSpeed(),
                    "行驶中门未关: 速度 " + t.getSpeed() + "km/h"));
        }

        // Rule 4: SENSOR_FAULT (max-min > threshold)
        if (maxTemp != null && minTemp != null
                && maxTemp.subtract(minTemp).compareTo(SENSOR_DEVIATION_LIMIT) > 0) {
            alerts.add(buildAlert(t.getDeviceSn(), IotAlert.TYPE_SENSOR_FAULT, IotAlert.LEVEL_INFO,
                    SENSOR_DEVIATION_LIMIT.toPlainString(),
                    maxTemp.subtract(minTemp).toPlainString(),
                    "传感器偏差过大: " + minTemp + "→" + maxTemp));
        }

        return alerts;
    }

    @Override
    public List<IotAlert> evaluateAndPersist(IotTelemetry telemetry) {
        List<IotAlert> alerts = evaluate(telemetry);
        for (IotAlert a : alerts) {
            iotAlertMapper.insert(a);
        }
        return alerts;
    }

    @Override
    public int sweepOfflineDevices(int maxStaleMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(maxStaleMinutes);
        List<IotDevice> online = iotDeviceMapper.findOnlineByType(IotDevice.TYPE_COLD_TRUCK);
        int marked = 0;
        for (IotDevice d : online) {
            if (d.getLastOnlineTime() != null && d.getLastOnlineTime().isBefore(cutoff)) {
                IotAlert a = buildAlert(d.getDeviceSn(), IotAlert.TYPE_OFFLINE, IotAlert.LEVEL_CRITICAL,
                        maxStaleMinutes + "min", "stale",
                        "设备离线 " + maxStaleMinutes + "+ 分钟");
                iotAlertMapper.insert(a);
                // Flip device status to offline
                iotDeviceMapper.update(null,
                        new LambdaUpdateWrapper<IotDevice>()
                                .eq(IotDevice::getId, d.getId())
                                .set(IotDevice::getStatus, IotDevice.STATUS_OFFLINE));
                marked++;
            }
        }
        return marked;
    }

    // ==== helpers ====

    private static IotAlert buildAlert(String sn, String type, int level,
                                        String threshold, String actual, String msg) {
        IotAlert a = new IotAlert();
        a.setDeviceSn(sn);
        a.setAlertType(type);
        a.setAlertLevel(level);
        a.setThresholdValue(threshold);
        a.setActualValue(actual);
        a.setMessage(msg);
        a.setHandled(0);
        return a;
    }

    private static BigDecimal maxNotNull(BigDecimal[] vals) {
        BigDecimal max = null;
        for (BigDecimal v : vals) {
            if (v == null) continue;
            if (max == null || v.compareTo(max) > 0) max = v;
        }
        return max;
    }

    private static BigDecimal minNotNull(BigDecimal[] vals) {
        BigDecimal min = null;
        for (BigDecimal v : vals) {
            if (v == null) continue;
            if (min == null || v.compareTo(min) < 0) min = v;
        }
        return min;
    }
}
