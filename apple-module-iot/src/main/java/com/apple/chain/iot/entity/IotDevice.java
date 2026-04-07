package com.apple.chain.iot.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * M3 IoT 设备. Table: iot_device (V21).
 */
@Getter
@Setter
@TableName("iot_device")
public class IotDevice extends BaseEntity {

    private String deviceSn;
    private String deviceType;
    private String vehicleNo;
    private Long warehouseId;
    private Integer status;
    private LocalDateTime lastOnlineTime;

    public static final String TYPE_COLD_TRUCK   = "COLD_TRUCK";
    public static final String TYPE_WAREHOUSE    = "WAREHOUSE";
    public static final String TYPE_PRE_COOLING  = "PRE_COOLING";

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ONLINE  = 1;
    public static final int STATUS_FAULT   = 2;
}
