package com.apple.chain.coldchain.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("cc_temperature_record")
public class TemperatureRecord extends BaseEntity {

    private Long taskId;
    private Long vehicleId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String location;
    private LocalDateTime recordTime;
    /** 0=normal, 1=alarm */
    private Integer isAlarm;
    private String alarmMsg;
}
