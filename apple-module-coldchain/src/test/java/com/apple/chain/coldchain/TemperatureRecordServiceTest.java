package com.apple.chain.coldchain;

import com.apple.chain.coldchain.entity.TemperatureRecord;
import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.mapper.TemperatureRecordMapper;
import com.apple.chain.coldchain.mapper.TransportTaskMapper;
import com.apple.chain.coldchain.service.impl.TemperatureRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("温度记录服务 - 告警判断测试")
class TemperatureRecordServiceTest {

    @Mock
    private TemperatureRecordMapper temperatureRecordMapper;

    @Mock
    private TransportTaskMapper transportTaskMapper;

    private TemperatureRecordServiceImpl temperatureRecordService;

    @BeforeEach
    void setUp() throws Exception {
        temperatureRecordService = new TemperatureRecordServiceImpl(transportTaskMapper);
        Field baseMapperField = temperatureRecordService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(temperatureRecordService, temperatureRecordMapper);
    }

    @Nested
    @DisplayName("createRecord - 温度告警判断")
    class CreateRecord {

        @Test
        @DisplayName("温度超出上限(>4)触发告警 - 默认阈值")
        void temperatureAboveMax_defaultThreshold_shouldAlarm() {
            // task=null, so default: lower=0, upper=4
            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("5.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(null);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(1);
            assertThat(result.getAlarmMsg()).contains("温度异常");
        }

        @Test
        @DisplayName("温度低于下限(<0)触发告警 - 默认阈值")
        void temperatureBelowMin_defaultThreshold_shouldAlarm() {
            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("-1.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(null);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(1);
            assertThat(result.getAlarmMsg()).contains("温度异常");
        }

        @Test
        @DisplayName("温度在范围内(2.0)不触发告警 - 默认阈值")
        void temperatureWithinRange_defaultThreshold_noAlarm() {
            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("2.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(null);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(0);
            assertThat(result.getAlarmMsg()).isNull();
        }

        @Test
        @DisplayName("温度在边界上限(4.0)不触发告警 - 默认阈值")
        void temperatureAtUpperBound_defaultThreshold_noAlarm() {
            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("4.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(null);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(0);
        }

        @Test
        @DisplayName("温度在边界下限(0.0)不触发告警 - 默认阈值")
        void temperatureAtLowerBound_defaultThreshold_noAlarm() {
            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("0.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(null);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(0);
        }

        @Test
        @DisplayName("任务有要求温度时使用任务温度范围(要求2.0, 容差2.0 => [0,4])")
        void temperatureWithTaskRequiredTemp_withinRange() {
            TransportTask task = new TransportTask();
            task.setRequiredTemp(new BigDecimal("2.0"));

            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("3.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(task);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            // range = [0.0, 4.0], 3.0 within range
            assertThat(result.getIsAlarm()).isEqualTo(0);
        }

        @Test
        @DisplayName("任务有要求温度时温度超出上限触发告警(要求2.0, 容差2.0 => 超过4.0)")
        void temperatureWithTaskRequiredTemp_aboveRange() {
            TransportTask task = new TransportTask();
            task.setRequiredTemp(new BigDecimal("2.0"));

            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("5.0"));

            given(transportTaskMapper.selectById(1L)).willReturn(task);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            // range = [0.0, 4.0], 5.0 > 4.0 => alarm
            assertThat(result.getIsAlarm()).isEqualTo(1);
            assertThat(result.getAlarmMsg()).contains("4.0");
        }

        @Test
        @DisplayName("任务有要求温度时温度低于下限触发告警(要求2.0, 容差2.0 => 低于0.0)")
        void temperatureWithTaskRequiredTemp_belowRange() {
            TransportTask task = new TransportTask();
            task.setRequiredTemp(new BigDecimal("2.0"));

            TemperatureRecord record = new TemperatureRecord();
            record.setTaskId(1L);
            record.setTemperature(new BigDecimal("-0.5"));

            given(transportTaskMapper.selectById(1L)).willReturn(task);
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            // range = [0.0, 4.0], -0.5 < 0.0 => alarm
            assertThat(result.getIsAlarm()).isEqualTo(1);
        }

        @Test
        @DisplayName("无taskId且无温度时不触发告警检查")
        void noTaskIdNoTemperature_noAlarmCheck() {
            TemperatureRecord record = new TemperatureRecord();
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(0);
        }

        @Test
        @DisplayName("isAlarm未设置时默认为0")
        void isAlarmDefaultsToZero() {
            TemperatureRecord record = new TemperatureRecord();
            record.setTemperature(new BigDecimal("2.0"));
            given(temperatureRecordMapper.insert(any(TemperatureRecord.class))).willReturn(1);

            TemperatureRecord result = temperatureRecordService.createRecord(record);

            assertThat(result.getIsAlarm()).isEqualTo(0);
        }
    }
}
