package com.apple.chain.iot.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * M3 IoT 告警. Table: iot_alert (V21).
 */
@Getter
@Setter
@TableName("iot_alert")
public class IotAlert extends BaseEntity {

    private String deviceSn;
    private String alertType;
    private Integer alertLevel;
    private String thresholdValue;
    private String actualValue;
    private String message;
    private Integer handled;

    public static final String TYPE_TEMP_HIGH    = "TEMP_HIGH";
    public static final String TYPE_TEMP_LOW     = "TEMP_LOW";
    public static final String TYPE_DOOR_ANOMALY = "DOOR_ANOMALY";
    public static final String TYPE_OFFLINE      = "OFFLINE";
    public static final String TYPE_SENSOR_FAULT = "SENSOR_FAULT";

    public static final int LEVEL_INFO     = 1;
    public static final int LEVEL_WARN     = 2;
    public static final int LEVEL_CRITICAL = 3;
}
