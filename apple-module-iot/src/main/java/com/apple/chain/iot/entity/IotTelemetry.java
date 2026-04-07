package com.apple.chain.iot.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * M3 IoT 遥测. Table: iot_telemetry (V21).
 */
@Getter
@Setter
@TableName("iot_telemetry")
public class IotTelemetry extends BaseEntity {

    private String deviceSn;
    private LocalDateTime collectTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;
    private BigDecimal speed;
    private BigDecimal temp1;
    private BigDecimal temp2;
    private BigDecimal temp3;
    private BigDecimal temp4;
    private BigDecimal humidity;
    private Integer doorStatus;
    private String signalType;
}
