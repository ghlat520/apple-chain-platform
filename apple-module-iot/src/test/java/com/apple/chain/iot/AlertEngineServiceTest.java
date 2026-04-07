package com.apple.chain.iot;

import com.apple.chain.iot.entity.IotAlert;
import com.apple.chain.iot.entity.IotTelemetry;
import com.apple.chain.iot.mapper.IotAlertMapper;
import com.apple.chain.iot.mapper.IotDeviceMapper;
import com.apple.chain.iot.service.impl.AlertEngineServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("M3 AlertEngineService 单元测试")
class AlertEngineServiceTest {

    @Mock private IotAlertMapper iotAlertMapper;
    @Mock private IotDeviceMapper iotDeviceMapper;

    @InjectMocks
    private AlertEngineServiceImpl engine;

    private IotTelemetry baseSample() {
        IotTelemetry t = new IotTelemetry();
        t.setDeviceSn("TRUCK-001");
        t.setTemp1(new BigDecimal("2.0"));
        t.setTemp2(new BigDecimal("2.0"));
        t.setTemp3(new BigDecimal("2.0"));
        t.setTemp4(new BigDecimal("2.0"));
        t.setSpeed(new BigDecimal("50"));
        t.setDoorStatus(0);
        return t;
    }

    @Test
    @DisplayName("正常温度范围 → 无告警")
    void normalTemperature_noAlert() {
        List<IotAlert> alerts = engine.evaluate(baseSample());
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Rule 1: 温度过高 > 4°C → TEMP_HIGH")
    void rule1_tempHigh() {
        IotTelemetry t = baseSample();
        t.setTemp1(new BigDecimal("6.5"));
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).extracting(IotAlert::getAlertType)
                .contains(IotAlert.TYPE_TEMP_HIGH);
    }

    @Test
    @DisplayName("Rule 2: 温度过低 < 0°C → TEMP_LOW")
    void rule2_tempLow() {
        IotTelemetry t = baseSample();
        t.setTemp2(new BigDecimal("-1.0"));
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).extracting(IotAlert::getAlertType)
                .contains(IotAlert.TYPE_TEMP_LOW);
    }

    @Test
    @DisplayName("Rule 3: 行驶中开门 → DOOR_ANOMALY")
    void rule3_doorAnomaly() {
        IotTelemetry t = baseSample();
        t.setDoorStatus(1);
        t.setSpeed(new BigDecimal("60"));
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).extracting(IotAlert::getAlertType)
                .contains(IotAlert.TYPE_DOOR_ANOMALY);
    }

    @Test
    @DisplayName("Rule 3: 停止时开门 → 不告警")
    void rule3_doorOpenWhileStopped_noAlert() {
        IotTelemetry t = baseSample();
        t.setDoorStatus(1);
        t.setSpeed(BigDecimal.ZERO);
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).extracting(IotAlert::getAlertType)
                .doesNotContain(IotAlert.TYPE_DOOR_ANOMALY);
    }

    @Test
    @DisplayName("Rule 4: 传感器偏差 > 1°C → SENSOR_FAULT")
    void rule4_sensorFault() {
        IotTelemetry t = baseSample();
        t.setTemp1(new BigDecimal("2.0"));
        t.setTemp2(new BigDecimal("2.0"));
        t.setTemp3(new BigDecimal("2.0"));
        t.setTemp4(new BigDecimal("3.5")); // spread 1.5 > 1.0
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).extracting(IotAlert::getAlertType)
                .contains(IotAlert.TYPE_SENSOR_FAULT);
    }

    @Test
    @DisplayName("多条规则同时触发 → 返回多条告警")
    void multipleRules() {
        IotTelemetry t = baseSample();
        t.setTemp1(new BigDecimal("-0.5")); // TEMP_LOW
        t.setTemp4(new BigDecimal("5.5"));  // TEMP_HIGH + SENSOR_FAULT
        t.setDoorStatus(1);
        t.setSpeed(new BigDecimal("40"));   // DOOR_ANOMALY
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).extracting(IotAlert::getAlertType)
                .contains(IotAlert.TYPE_TEMP_LOW,
                          IotAlert.TYPE_TEMP_HIGH,
                          IotAlert.TYPE_DOOR_ANOMALY,
                          IotAlert.TYPE_SENSOR_FAULT);
    }

    @Test
    @DisplayName("null 遥测 → 空 alerts")
    void nullTelemetry() {
        assertThat(engine.evaluate(null)).isEmpty();
    }

    @Test
    @DisplayName("部分 null 温度传感器 → 不抛异常")
    void partialNullSensors() {
        IotTelemetry t = baseSample();
        t.setTemp1(null);
        t.setTemp2(null);
        // Only temp3, temp4 populated
        List<IotAlert> alerts = engine.evaluate(t);
        assertThat(alerts).isEmpty();
    }
}
